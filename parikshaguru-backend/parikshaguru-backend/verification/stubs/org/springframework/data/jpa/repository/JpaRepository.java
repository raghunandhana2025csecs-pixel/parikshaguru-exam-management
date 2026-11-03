package org.springframework.data.jpa.repository;
import java.util.*; import org.springframework.data.domain.Sort;
public interface JpaRepository<T,ID> { <S extends T> S save(S e); <S extends T> List<S> saveAll(Iterable<S> e); Optional<T> findById(ID id); List<T> findAll(); List<T> findAll(Sort s);
  void delete(T e); long count(); T getReferenceById(ID id); boolean existsById(ID id); }
