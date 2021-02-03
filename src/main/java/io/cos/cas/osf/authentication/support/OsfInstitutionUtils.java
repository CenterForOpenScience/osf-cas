package io.cos.cas.osf.authentication.support;

import io.cos.cas.osf.dao.JpaOsfDao;
import io.cos.cas.osf.model.OsfInstitution;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OsfInstitutionUtils}.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@Slf4j
public final class OsfInstitutionUtils {

    public static boolean validateInstitutionForLogin(final JpaOsfDao jpaOsfDao, final String id) {
        final OsfInstitution institution = jpaOsfDao.findOneInstitutionById(id);
        return institution != null && institution.getDelegationProtocol() != null;
    }

    public static Map<String, String> getInstitutionLoginUrlMap(
            final JpaOsfDao jpaOsfDao,
            final String target,
            final String id
    ) {
        List<OsfInstitution> institutionList = new LinkedList<>();
        if (id == null || id.isEmpty()) {
            institutionList = jpaOsfDao.findAllInstitutions();
        } else {
            final OsfInstitution institution = jpaOsfDao.findOneInstitutionById(id);
            if (institution != null) {
                institutionList.add(institution);
            } else {
                institutionList = jpaOsfDao.findAllInstitutions();
            }
        }
        final Map<String, String> institutionLoginUrlMap = new HashMap<>();
        for (final OsfInstitution institution: institutionList) {
            final DelegationProtocol delegationProtocol = institution.getDelegationProtocol();
            if (DelegationProtocol.SAML_SHIB.equals(delegationProtocol)) {
                institutionLoginUrlMap.put(
                        institution.getLoginUrl() + "&target=" + target + '#' + institution.getId(),
                        institution.getName()
                );
            } else if (DelegationProtocol.CAS_PAC4J.equals(delegationProtocol)) {
                institutionLoginUrlMap.put(institution.getInstitutionId(), institution.getName());
            }
        }
        return institutionLoginUrlMap;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map) {
        final List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(final Map.Entry<K, V> e1, final Map.Entry<K, V> e2) {
                return (e1.getValue()).compareTo(e2.getValue());
            }
        });
        final Map<K, V> result = new LinkedHashMap<>();
        for (final Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
