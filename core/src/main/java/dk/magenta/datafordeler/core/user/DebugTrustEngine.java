package dk.magenta.datafordeler.core.user;

import com.google.common.base.Strings;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.KeyAlgorithmCriterion;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.algorithm.AlgorithmSupport;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class DebugTrustEngine extends ExplicitKeySignatureTrustEngine {
    public DebugTrustEngine(@Nonnull CredentialResolver resolver, @Nonnull KeyInfoCredentialResolver keyInfoResolver) {
        super(resolver, keyInfoResolver);
    }

    private final Logger log = LoggerFactory.getLogger(ExplicitKeySignatureTrustEngine.class);

    protected boolean doValidate(@Nonnull final Signature signature, @Nullable final CriteriaSet trustBasisCriteria) throws org.opensaml.security.SecurityException {
        CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.addAll(trustBasisCriteria);
        if (!criteriaSet.contains(UsageCriterion.class)) {
            criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
        }

        String signatureAlg = signature.getSignatureAlgorithm();
        String jcaAlgorithm = signatureAlg != null ? AlgorithmSupport.getKeyAlgorithm(signatureAlg) : null;
        if (!Strings.isNullOrEmpty(jcaAlgorithm)) {
            criteriaSet.add(new KeyAlgorithmCriterion(jcaAlgorithm), true);
        }

        ArrayList<Credential> trustedCredentials;
        try {
            trustedCredentials = new ArrayList<>();
            log.info("Finding trusted credentials");
            for (Credential c : this.getCredentialResolver().resolve(criteriaSet)) {
                trustedCredentials.add(c);
            }
        } catch (ResolverException e) {
            throw new SecurityException("Error resolving trusted credentials", e);
        }
        this.log.info("There are "+trustedCredentials.size()+" trusted credentials");

        if (this.validate(signature, trustedCredentials)) {
            return true;
        } else {
            this.log.info("Attempting to verify signature using trusted credentials");

            for(Credential trustedCredential : trustedCredentials) {
                assert trustedCredential != null;

                if (this.verifySignature(signature, trustedCredential)) {
                    this.log.info("Successfully verified signature using resolved trusted credential");
                    return true;
                }
            }

            this.log.info("Failed to verify signature using either KeyInfo-derived or directly trusted credentials");
            return false;
        }
    }
}
