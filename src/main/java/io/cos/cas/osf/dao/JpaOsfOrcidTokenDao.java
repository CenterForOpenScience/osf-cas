package io.cos.cas.osf.dao;

import io.cos.cas.osf.orcidtoken.OsfOrcidToken;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

/**
 * This is {@link JpaOsfOrcidTokenDao}.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Slf4j
@NoArgsConstructor
@Transactional(transactionManager = "orcidTokenTransactionManager")
public class JpaOsfOrcidTokenDao implements OsfOrcidTokenDao {

    @NotNull
    @PersistenceContext(unitName = "orcidTokenEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public OsfOrcidToken findByOrcidId(final String orcidId) {
        try {
            final TypedQuery<OsfOrcidToken> query = entityManager.createQuery(
                    "select t from OsfOrcidToken t where t.orcidId = :orcidId",
                    OsfOrcidToken.class
            );
            query.setParameter("orcidId", orcidId);
            return query.getSingleResult();
        } catch (final PersistenceException e) {
            return null;
        }
    }

    @Override
    public OsfOrcidToken upsertToken(
            final String orcidId,
            final String accessToken,
            final String refreshToken,
            final String scope
    ) {
        OsfOrcidToken token = findByOrcidId(orcidId);
        if (token == null) {
            token = new OsfOrcidToken();
            token.setOrcidId(orcidId);
        }
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setScope(scope);
        return entityManager.merge(token);
    }

    @Override
    public void deleteByOrcidId(final String orcidId) {
        final OsfOrcidToken token = findByOrcidId(orcidId);
        if (token != null) {
            entityManager.remove(entityManager.contains(token) ? token : entityManager.merge(token));
        } else {
            LOGGER.debug("No stored ORCID token found for orcid id [{}]; nothing to delete", orcidId);
        }
    }
}
