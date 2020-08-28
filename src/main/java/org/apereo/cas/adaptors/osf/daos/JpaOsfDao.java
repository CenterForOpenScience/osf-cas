package org.apereo.cas.adaptors.osf.daos;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.osf.models.OsfEmail;
import org.apereo.cas.adaptors.osf.models.OsfGuid;
import org.apereo.cas.adaptors.osf.models.OsfUser;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

/**
 * This is {@link JpaOsfDao}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
@Transactional(transactionManager = "jpaOsfDaoTransactionManager")
@Slf4j
public class JpaOsfDao extends AbstractOsfDao {

    @NotNull
    @PersistenceContext(unitName = "jpaOsfDaoEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    protected OsfUser findOneUserByUsername(final String username) {
        try {
            final TypedQuery<OsfUser> query = entityManager.createQuery(
                    "select u from OsfUser u where u.username = :username",
                    OsfUser.class
            );
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    protected OsfEmail findOneEmailByAddress(final String emailAddress) {
        try {
            final TypedQuery<OsfEmail> query = entityManager.createQuery(
                    "select e from OsfEmail e where e.address = :address",
                    OsfEmail.class
            );
            query.setParameter("address", emailAddress);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public OsfGuid findGuidByUser(final OsfUser osfUser) {
        try {
            final TypedQuery<OsfGuid> query = entityManager.createQuery(
                    "select g from OsfGuid g where"
                            + " g.objectId = :userId"
                            + " and g.djangoContentType.appLabel = :appLable"
                            + " and g.djangoContentType.model = :model",
                    OsfGuid.class
            );
            query.setParameter("userId", osfUser.getId());
            query.setParameter("appLable", "osf");
            query.setParameter("model", "osfuser");
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }
}
