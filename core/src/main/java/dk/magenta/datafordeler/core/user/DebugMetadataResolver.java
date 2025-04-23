package dk.magenta.datafordeler.core.user;

import com.google.common.collect.Iterables;
import net.shibboleth.shared.annotation.constraint.NotEmpty;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import net.shibboleth.shared.resolver.ResolverSupport;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.criterion.SatisfyAnyCriterion;
import org.opensaml.profile.criterion.ProfileRequestContextCriterion;
import org.opensaml.saml.metadata.criteria.entity.EvaluableEntityDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;

public class DebugMetadataResolver extends FilesystemMetadataResolver {

    private final Logger log = LoggerFactory.getLogger(DebugMetadataResolver.class);

    public DebugMetadataResolver(@Nonnull File file) throws ResolverException {
        super(file);
    }

    public DebugMetadataResolver(@Nullable Timer backgroundTaskTimer, @Nonnull File file) throws ResolverException {
        super(backgroundTaskTimer, file);
    }


    @Nonnull
    public Iterable<EntityDescriptor> resolve(@Nullable final CriteriaSet criteria) throws ResolverException {
        ArrayList<EntityDescriptor> r = new ArrayList<>();
        for (EntityDescriptor e : super.resolve(criteria)) {
            r.add(e);
        }
        this.log.info("Resolved " + r.size() + " for " + criteria);
        return r;
    }


    @Nonnull
    @Override
    protected Iterable<EntityDescriptor> doResolve(@Nullable CriteriaSet criteria) throws ResolverException {
        this.checkComponentActive();
        EntityIdCriterion entityIdCriterion = criteria != null ? (EntityIdCriterion)criteria.get(EntityIdCriterion.class) : null;
        if (entityIdCriterion != null) {
            Iterable<EntityDescriptor> entityIdcandidates = this.lookupEntityID(entityIdCriterion.getEntityId());
            this.log.info("{} Resolved {} candidates via EntityIdCriterion: {}", new Object[]{this.getLogPrefix(), Iterables.size(entityIdcandidates), entityIdCriterion});
            for (EntityDescriptor entity : entityIdcandidates) {
                this.log.info("EntityId: "+entity.getEntityID());
            }
            this.log.info("Criterion: "+entityIdCriterion.getEntityId());

            ArrayList<EntityDescriptor> r = new ArrayList<>();
            for (EntityDescriptor e : this.predicateFilterCandidates(entityIdcandidates, criteria, false)) {
                r.add(e);
            }
            this.log.info("Resolved "+r.size()+" candidates");
            return r;
        } else {
            Optional<Set<EntityDescriptor>> indexedCandidates = this.lookupByIndexes(criteria);
            if (indexedCandidates.isPresent()) {
                this.log.info("{} Resolved {} candidates via secondary index lookup", this.getLogPrefix(), Iterables.size((Iterable)indexedCandidates.get()));
            } else {
                this.log.info("{} Resolved no candidates via secondary index lookup (Optional indicated result was absent)", this.getLogPrefix());
            }


            if (indexedCandidates.isPresent()) {
                this.log.info("{} Performing predicate filtering of resolved secondary indexed candidates", this.getLogPrefix());
                return this.predicateFilterCandidates((Iterable)indexedCandidates.get(), criteria, false);
            } else if (this.isResolveViaPredicatesOnly()) {
                this.log.info("{} Performing predicate filtering of entire metadata collection", this.getLogPrefix());
                return this.predicateFilterCandidates(this, criteria, true);
            } else {
                this.log.info("{} Resolved no secondary indexed candidates, returning empty result", this.getLogPrefix());
                return CollectionSupport.emptySet();
            }
        }
    }



    @Nonnull
    protected Iterable<EntityDescriptor> predicateFilterCandidates(@Nonnull final Iterable<EntityDescriptor> candidates, @Nullable final CriteriaSet criteria, final boolean onEmptyPredicatesReturnEmpty) throws ResolverException {
        if (!candidates.iterator().hasNext()) {
            this.log.info("{} Candidates iteration was empty, nothing to filter via predicates", this.getLogPrefix());
            return CollectionSupport.emptySet();
        } else {
            this.log.info("{} Attempting to filter candidate EntityDescriptors via resolved Predicates", this.getLogPrefix());
            Set<Predicate<EntityDescriptor>> predicates = ResolverSupport.getPredicates(criteria, EvaluableEntityDescriptorCriterion.class, this.getCriterionPredicateRegistry());
            this.log.info("{} Resolved {} Predicates: {}", new Object[]{this.getLogPrefix(), predicates.size(), predicates});
            SatisfyAnyCriterion satisfyAnyCriterion = criteria != null ? (SatisfyAnyCriterion)criteria.get(SatisfyAnyCriterion.class) : null;
            boolean satisfyAny;
            if (satisfyAnyCriterion != null) {
                this.log.info("{} CriteriaSet contained SatisfyAnyCriterion", this.getLogPrefix());
                satisfyAny = satisfyAnyCriterion.isSatisfyAny();
            } else {
                this.log.info("{} CriteriaSet did NOT contain SatisfyAnyCriterion", this.getLogPrefix());
                satisfyAny = this.isSatisfyAnyPredicates();
            }

            this.log.info("{} Effective satisyAny value: {}", this.getLogPrefix(), satisfyAny);
            Iterable<EntityDescriptor> result = ResolverSupport.getFilteredIterable(candidates, predicates, satisfyAny, onEmptyPredicatesReturnEmpty);
            this.log.info("{} After predicate filtering {} EntityDescriptors remain", this.getLogPrefix(), Iterables.size(result));


            return result;
        }
    }

}
