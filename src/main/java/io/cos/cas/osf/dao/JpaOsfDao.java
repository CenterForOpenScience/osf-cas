package io.cos.cas.osf.dao;

import io.cos.cas.osf.model.OsfEmail;
import io.cos.cas.osf.model.OsfGuid;
import io.cos.cas.osf.model.OsfInstitution;
import io.cos.cas.osf.model.OsfOAuth20App;
import io.cos.cas.osf.model.OsfOAuth20Pat;
import io.cos.cas.osf.model.OsfOAuth20PatScopes;
import io.cos.cas.osf.model.OsfOAuth20Scope;
import io.cos.cas.osf.model.OsfTotp;
import io.cos.cas.osf.model.OsfUser;

import lombok.NoArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * This is {@link JpaOsfDao}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@NoArgsConstructor
@Transactional(transactionManager = "jpaOsfDaoTransactionManager")
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

    @Override
    public OsfTotp findOneTotpByOwnerId(final Integer ownerId) {
        try {
            final TypedQuery<OsfTotp> query = entityManager.createQuery(
                    "select p from OsfTotp p where p.owner.id = :ownerId",
                    OsfTotp.class
            );
            query.setParameter("ownerId", ownerId);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public OsfInstitution findOneInstitutionById(final String id) {
        try {
            final TypedQuery<OsfInstitution> query = entityManager.createQuery(
                    "select i from OsfInstitution i where i.institutionId = :id and i.deleted = false and i.dateDeactivated is null",
                    OsfInstitution.class
            );
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public List<OsfInstitution> findAllInstitutions() {
        try {
            final TypedQuery<OsfInstitution> query = entityManager.createQuery(
                    "select i from OsfInstitution i "
                            + "where (not i.delegationProtocol = '') and i.deleted = false and i.dateDeactivated is null",
                    OsfInstitution.class
            );
            return query.getResultList();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public OsfOAuth20Scope findOneScopeByScopeName(final String name) {
        try {
            final TypedQuery<OsfOAuth20Scope> query = entityManager.createQuery(
                    "select s from OsfOAuth20Scope s where s.name = :name",
                    OsfOAuth20Scope.class
            );
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public OsfOAuth20Scope findOneScopeByScopePk(final Integer scopePk) {
        try {
            final TypedQuery<OsfOAuth20Scope> query = entityManager.createQuery(
                    "select s from OsfOAuth20Scope s where s.id = :id",
                    OsfOAuth20Scope.class
            );
            query.setParameter("id", scopePk);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public OsfOAuth20Pat findOnePatByTokenId(final String tokenId) {
        try {
            final TypedQuery<OsfOAuth20Pat> query = entityManager.createQuery(
                    "select t from OsfOAuth20Pat t where t.tokenId = :tokenId",
                    OsfOAuth20Pat.class
            );
            query.setParameter("tokenId", tokenId);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public List<OsfOAuth20PatScopes> findAllPatScopesByTokenPk(Integer tokenPk) {
        try {
            final TypedQuery<OsfOAuth20PatScopes> query = entityManager.createQuery(
                    "select m from OsfOAuth20PatScopes m where m.tokenPk = :tokenPk",
                    OsfOAuth20PatScopes.class
            );
            query.setParameter("tokenPk", tokenPk);
            return query.getResultList();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public List<OsfOAuth20App> findAllOsfOAuth20Apps() {
        try {
            final TypedQuery<OsfOAuth20App> query = entityManager.createQuery(
                    "select a from OsfOAuth20App a where a.isActive = true",
                    OsfOAuth20App.class
            );
            return query.getResultList();
        } catch (final PersistenceException e) {
            return null;
        }
    }
}
