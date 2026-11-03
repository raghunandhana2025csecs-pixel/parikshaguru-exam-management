package org.springframework.data.domain;
import java.util.*; import java.util.function.Function;
public interface Page<T> { <U> Page<U> map(Function<? super T,? extends U> f); List<T> getContent(); int getNumber(); int getSize(); long getTotalElements(); int getTotalPages(); }
