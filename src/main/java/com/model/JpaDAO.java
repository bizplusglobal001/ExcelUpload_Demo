package com.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JpaDAO<E> {

    protected EntityManagerFactory entityManagerFactory = HibernateUtil.getEntityManagerFactory();

    //등록(C)
    public E create(E entity) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        try {
            entityTransaction.begin();
            entityManager.persist(entity);
            entityTransaction.commit();
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            entityTransaction.rollback();
            return null;
        } finally {
            entityManager.close();
        }
    }

    //수정(U)
    public E update(E entity) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        try {
            entityTransaction.begin();
            entityManager.merge(entity);
            entityTransaction.commit();
            ;
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            entityTransaction.rollback();
            return null;
        } finally {
            entityManager.close();
        }
    }

    //조회(F)
    public E find(Class<E> type, Object id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        E entity = entityManager.find(type, id);

        if (entity == null) {
            return null;
        } else {
            return entity;
        }
    }


    //삭제(D)
    public boolean delete(Class<E> type, Object id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        try {
            Object references = entityManager.getReference(type, id);
            entityTransaction.begin();
            entityManager.remove(references);
            entityTransaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            entityTransaction.rollback();
            return false;
        } finally {
            entityManager.close();
        }
    }

    //리스트
    public List<E> findWithNamedQuery(String queryName) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Query query = entityManager.createNamedQuery(queryName);
        List<E> result = query.getResultList();

        entityManager.close();

        return result;
    }


    public List<E> findWithNamedQuery(String queryName, Map<String, Object> parameters) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createNamedQuery(queryName);

        Set<Map.Entry<String, Object>> setParameters = parameters.entrySet();

        for (Map.Entry<String, Object> entry : setParameters) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getResultList();
    }

    public List<E> findWithNamedQuery(String queryName, String paramName, Object paramValue) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Query query = entityManager.createNamedQuery(queryName);
        query.setParameter(paramName, paramValue);
        List<E> result = query.getResultList();

        entityManager.close();

        return result;
    }


}
