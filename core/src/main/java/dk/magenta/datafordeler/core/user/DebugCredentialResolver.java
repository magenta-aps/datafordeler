package dk.magenta.datafordeler.core.user;

import com.google.common.collect.Iterables;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.logic.PredicateSupport;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.Criterion;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.criteria.impl.EvaluableCredentialCriteriaRegistry;
import org.opensaml.security.credential.criteria.impl.EvaluableCredentialCriterion;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class DebugCredentialResolver extends MetadataCredentialResolver {

    private final Logger log = LoggerFactory.getLogger(DebugMetadataResolver.class);

    @Nonnull
    public Iterable<Credential> resolve(@Nullable final CriteriaSet criteriaSet) throws ResolverException {
        Iterable<Credential> storeCandidates = this.resolveFromSource(criteriaSet);
        this.log.info("storeCandidates: "+Iterables.size(storeCandidates));
        Set<Predicate<Credential>> predicates = this.getPredicates(criteriaSet);
        log.info("predicates: "+predicates.size());
        for (Predicate<Credential> predicate : predicates) {
            log.info(predicate.toString());
        }
        if (predicates.isEmpty()) {
            return storeCandidates;
        } else {
            Predicate<Credential> aggregatePredicate = null;
            if (this.isSatisfyAllPredicates()) {
                aggregatePredicate = PredicateSupport.and(predicates);
            } else {
                aggregatePredicate = PredicateSupport.or(predicates);
            }

            Objects.requireNonNull(aggregatePredicate);
            return Iterables.filter(storeCandidates, aggregatePredicate::test);
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
