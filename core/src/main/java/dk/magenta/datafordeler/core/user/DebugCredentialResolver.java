package dk.magenta.datafordeler.core.user;

import com.google.common.collect.Iterables;
import net.shibboleth.shared.annotation.constraint.NotEmpty;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.collection.LockableClassToInstanceMultiMap;
import net.shibboleth.shared.logic.PredicateSupport;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.Criterion;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLMDCredentialContext;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialContextSet;
import org.opensaml.security.credential.MutableCredential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.criteria.impl.EvaluableCredentialCriteriaRegistry;
import org.opensaml.security.credential.criteria.impl.EvaluableCredentialCriterion;
import org.opensaml.xmlsec.keyinfo.KeyInfoCriterion;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Predicate;

public class DebugCredentialResolver extends MetadataCredentialResolver {

    private final Logger log = LoggerFactory.getLogger(DebugMetadataResolver.class);

    @Nonnull
    public Iterable<Credential> resolve(@Nullable final CriteriaSet criteriaSet) throws ResolverException {
        Iterable<Credential> storeCandidates = this.resolveFromSource(criteriaSet);
        this.log.info("storeCandidates: "+Iterables.size(storeCandidates));
        Set<Predicate<Credential>> predicates = this.getPredicates(criteriaSet);
        log.info("predicates: "+predicates.size());
        if (predicates.isEmpty()) {
            log.info("No predicates found");
            return storeCandidates;
        } else {
            Predicate<Credential> aggregatePredicate = null;
            if (this.isSatisfyAllPredicates()) {
                log.info("Satisfy all predicates");
                aggregatePredicate = PredicateSupport.and(predicates);
            } else {
                log.info("Satisfy one predicate");
                aggregatePredicate = PredicateSupport.or(predicates);
            }

            for (Credential credential : storeCandidates) {
                log.info("-------------");
                log.info(credential.toString());
                log.info((credential.getPublicKey() != null ? credential.getPublicKey().getAlgorithm():null)+"/"+(credential.getPrivateKey()!=null?credential.getPrivateKey().getAlgorithm():null));
                log.info(credential.getUsageType()!=null?credential.getUsageType().getValue():null);
                log.info(credential.getEntityId());
                for (Predicate<Credential> p : predicates) {
                    log.info(p.toString());
                    log.info(p.test(credential)?"true":"false");
                }
            }

            Objects.requireNonNull(aggregatePredicate);
            return Iterables.filter(storeCandidates, aggregatePredicate::test);
        }
    }


    protected Collection<Credential> resolveFromMetadata(@Nullable final CriteriaSet criteriaSet, @Nonnull @NotEmpty final String entityID, @Nonnull final QName role, @Nullable final String protocol, @Nonnull final UsageType usage) throws ResolverException {
        this.log.info("Resolving credentials from metadata using entityID: {}, role: {}, protocol: {}, usage: {}", new Object[]{entityID, role, protocol, usage});
        LinkedHashSet<Credential> credentials = new LinkedHashSet(3);

        for (RoleDescriptor roleDescriptor : this.getRoleDescriptors(criteriaSet, entityID, role, protocol)) {
            assert roleDescriptor != null;
            log.info(roleDescriptor.toString());
            this.processRoleDescriptor(credentials, roleDescriptor, entityID, usage);
        }

        return credentials;
    }



    protected void processRoleDescriptor(@Nonnull final Collection<Credential> accumulator, @Nonnull final RoleDescriptor roleDescriptor, @Nullable final String entityID, @Nonnull final UsageType usage) throws ResolverException {
        log.info(roleDescriptor.getKeyDescriptors().size()+" key descriptors");
        for(KeyDescriptor keyDescriptor : roleDescriptor.getKeyDescriptors()) {
            UsageType mdUsage = keyDescriptor.getUse();
            if (mdUsage == null) {
                mdUsage = UsageType.UNSPECIFIED;
            }

            if (this.matchUsage(mdUsage, usage) && keyDescriptor.getKeyInfo() != null) {
                log.info("match usage and keyinfo is not null");
                this.extractCredentials(accumulator, keyDescriptor, entityID, mdUsage);
            } else {
                log.info("NO match usage and keyinfo is not null");
            }
        }

    }


    protected void extractCredentials(@Nonnull final Collection<Credential> accumulator, @Nonnull final KeyDescriptor keyDescriptor, @Nullable final String entityID, @Nonnull final UsageType mdUsage) throws ResolverException {
        LockableClassToInstanceMultiMap<Object> keyDescriptorObjectMetadata = keyDescriptor.getObjectMetadata();
        ReadWriteLock rwlock = keyDescriptorObjectMetadata.getReadWriteLock();

        try {
            rwlock.readLock().lock();
            List<Credential> cachedCreds = keyDescriptorObjectMetadata.get(Credential.class);
            log.info("cachedCreds: "+cachedCreds.size());
            if (!cachedCreds.isEmpty()) {
                this.log.debug("Resolved cached credentials from KeyDescriptor object metadata");
                accumulator.addAll(cachedCreds);
                return;
            }

            this.log.debug("Found no cached credentials in KeyDescriptor object metadata, resolving from KeyInfo");
        } finally {
            rwlock.readLock().unlock();
        }

        try {
            rwlock.writeLock().lock();
            List<Credential> cachedCreds = keyDescriptorObjectMetadata.get(Credential.class);
            if (cachedCreds.isEmpty()) {
                List<Credential> newCreds = new ArrayList();
                CriteriaSet critSet = new CriteriaSet();
                critSet.add(new KeyInfoCriterion(keyDescriptor.getKeyInfo()));

                log.info("creds:"+Iterables.size(this.getKeyInfoCredentialResolver().resolve(critSet)));
                for(Credential cred : this.getKeyInfoCredentialResolver().resolve(critSet)) {
                    if (cred instanceof MutableCredential) {
                        MutableCredential mutableCred = (MutableCredential)cred;
                        mutableCred.setEntityId(entityID);
                        mutableCred.setUsageType(mdUsage);
                    }

                    CredentialContextSet contextSet = cred.getCredentialContextSet();
                    if (contextSet != null) {
                        contextSet.add(new SAMLMDCredentialContext(keyDescriptor));
                    }

                    newCreds.add(cred);
                }
                log.info("newCreds: "+newCreds.size());

                keyDescriptorObjectMetadata.putAll(newCreds);
                accumulator.addAll(newCreds);
                return;
            }

            this.log.debug("Credentials were resolved and cached by another thread while this thread was waiting on the write lock");
            accumulator.addAll(cachedCreds);
        } finally {
            rwlock.writeLock().unlock();
        }

    }



    @Nonnull
    private Set<Predicate<Credential>> getPredicates(@Nullable final CriteriaSet criteriaSet) throws ResolverException {
        if (criteriaSet == null) {
            return CollectionSupport.emptySet();
        } else {
            Set<Predicate<Credential>> predicates = new HashSet(criteriaSet.size());

            for(Criterion criteria : criteriaSet) {
                assert criteria != null;

                if (criteria instanceof EvaluableCredentialCriterion) {
                    predicates.add((EvaluableCredentialCriterion)criteria);
                } else {
                    EvaluableCredentialCriterion evaluableCriteria;
                    try {
                        log.info(criteria.toString());
                        evaluableCriteria = EvaluableCredentialCriteriaRegistry.getEvaluator(criteria);
                    } catch (SecurityException e) {
                        throw new ResolverException("Exception obtaining EvaluableCredentialCriterion", e);
                    }

                    if (evaluableCriteria != null) {
                        predicates.add(evaluableCriteria);
                    }
                }
            }

            return predicates;
        }
    }
}
