package dk.magenta.datafordeler.core.user;

import com.google.common.collect.Iterables;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;

public class DebugMetadataResolver extends FilesystemMetadataResolver {

    private final Logger log = LoggerFactory.getLogger(DebugMetadataResolver.class);

    public DebugMetadataResolver(@Nonnull File file) throws ResolverException {
        super(file);
    }

    public DebugMetadataResolver(@Nullable Timer backgroundTaskTimer, @Nonnull File file) throws ResolverException {
        super(backgroundTaskTimer, file);
    }

    @Nonnull
    @Override
    protected Iterable<EntityDescriptor> doResolve(@Nullable CriteriaSet criteria) throws ResolverException {
        this.checkComponentActive();
        EntityIdCriterion entityIdCriterion = criteria != null ? (EntityIdCriterion)criteria.get(EntityIdCriterion.class) : null;
        if (entityIdCriterion != null) {
            Iterable<EntityDescriptor> entityIdcandidates = this.lookupEntityID(entityIdCriterion.getEntityId());
            this.log.info("{} Resolved {} candidates via EntityIdCriterion: {}", new Object[]{this.getLogPrefix(), Iterables.size(entityIdcandidates), entityIdCriterion});


            return this.predicateFilterCandidates(entityIdcandidates, criteria, false);
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
}
