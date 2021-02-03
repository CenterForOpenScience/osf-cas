package io.cos.cas.osf.dao;

import io.cos.cas.osf.model.OsfEmail;
import io.cos.cas.osf.model.OsfGuid;
import io.cos.cas.osf.model.OsfInstitution;
import io.cos.cas.osf.model.OsfTotp;
import io.cos.cas.osf.model.OsfUser;

import java.util.List;

/**
 * This is {@link AbstractOsfDao}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
public abstract class AbstractOsfDao {

    public OsfUser findOneUserByEmail(final String address) {

        final OsfUser osfUser = findOneUserByUsername(address);
        if (osfUser != null) {
            return osfUser;
        }

        final OsfEmail osfEmail = findOneEmailByAddress(address);
        return osfEmail != null ? osfEmail.getUser() : null;
    }

    public abstract OsfGuid findGuidByUser(OsfUser user);

    protected abstract OsfUser findOneUserByUsername(String username);

    protected abstract OsfEmail findOneEmailByAddress(String emailAddress);

    protected abstract OsfTotp findOneTotpByOwnerId(final Integer ownerId);

    protected abstract OsfInstitution findOneInstitutionById(final String id);

    protected abstract List<OsfInstitution> findAllInstitutions();
}
