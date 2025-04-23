package dk.magenta.datafordeler.core.user;

import com.google.common.collect.Iterables;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.logic.PredicateSupport;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.common.IsTimeboundSAMLObjectValidPredicate;
import org.opensaml.saml.saml2.common.SAML2Support;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class DebugRoleDescriptorResolver extends PredicateRoleDescriptorResolver {

    private static final Predicate<XMLObject> IS_VALID_PREDICATE = new IsTimeboundSAMLObjectValidPredicate();

    private MetadataResolver entityDescriptorResolver;

    public DebugRoleDescriptorResolver(@Nonnull MetadataResolver mdResolver) {
        super(mdResolver);
        this.entityDescriptorResolver = mdResolver;
    }

    private final Logger log = LoggerFactory.getLogger(DebugRoleDescriptorResolver.class);

    @Nonnull
    public Iterable<RoleDescriptor> resolve(@Nullable final CriteriaSet criteria) throws ResolverException {
        this.checkComponentActive();
        Iterable<EntityDescriptor> entityDescriptorsSource = this.entityDescriptorResolver.resolve(criteria);
        if (!entityDescriptorsSource.iterator().hasNext()) {
            this.log.info("Resolved no EntityDescriptors via underlying MetadataResolver, returning empty collection");
            return CollectionSupport.emptySet();
        } else {
            this.log.info("Resolved {} source EntityDescriptors", Iterables.size(entityDescriptorsSource));

            Predicate<? super RoleDescriptor> predicate = this.isRequireValidMetadata() ? IS_VALID_PREDICATE : PredicateSupport.alwaysTrue();
            if (this.haveRoleCriteria(criteria)) {
                Iterable<RoleDescriptor> candidates = this.getCandidatesByRoleAndProtocol(entityDescriptorsSource, criteria);
                this.log.info("Resolved {} RoleDescriptor candidates via role criteria, performing predicate filtering", Iterables.size(candidates));

                for (RoleDescriptor candidate : candidates) {
                    log.info(candidate.getDOM().getTagName());
                    log.info(candidate.getValidUntil().toString());
                    log.info(SAML2Support.isValid(candidate)?"true":"false");
                }
                Objects.requireNonNull(predicate);
                this.log.info(this.predicateFilterCandidates(Iterables.filter(candidates, IS_VALID_PREDICATE::test), criteria, false););


                return this.predicateFilterCandidates(Iterables.filter(candidates, predicate::test), criteria, false);
            } else if (this.isResolveViaPredicatesOnly()) {
                Iterable<RoleDescriptor> candidates = this.getAllCandidates(entityDescriptorsSource);
                this.log.info("Resolved {} RoleDescriptor total candidates for predicate-only resolution", Iterables.size(candidates));

                Objects.requireNonNull(predicate);
                return this.predicateFilterCandidates(Iterables.filter(candidates, predicate::test), criteria, true);
            } else {
                this.log.info("Found no role criteria and predicate-only resolution is disabled, returning empty collection");
                return CollectionSupport.emptySet();
            }
        }
    }
}
