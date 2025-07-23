package no.rutebanken.baba.organisation.m2m;

import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class TestM2MClientRepository implements M2MClientRepository {
    private final M2MClient client;

    public TestM2MClientRepository(M2MClient client) {
        this.client = client;
    }

    @Override
    public M2MClient getOneByPublicId(String id) {
        throw new NotImplementedException();
    }

    @Override
    public M2MClient getOneByPublicIdIfExists(String id) {
        return client;
    }

    @Override
    public List<M2MClient> findAll() {
        throw new NotImplementedException();
    }

    @Override
    public void flush() {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> S saveAndFlush(S entity) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAllInBatch(Iterable<M2MClient> entities) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAllInBatch() {
        throw new NotImplementedException();
    }

    @Override
    public M2MClient getOne(Long aLong) {
        throw new NotImplementedException();
    }

    @Override
    public M2MClient getById(Long aLong) {
        throw new NotImplementedException();
    }

    @Override
    public M2MClient getReferenceById(Long aLong) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> List<S> findAll(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> List<S> findAll(Example<S> example, Sort sort) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> List<S> saveAll(Iterable<S> entities) {
        throw new NotImplementedException();
    }

    @Override
    public List<M2MClient> findAllById(Iterable<Long> longs) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> S save(S entity) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<M2MClient> findById(Long aLong) {
        throw new NotImplementedException();
    }

    @Override
    public boolean existsById(Long aLong) {
        throw new NotImplementedException();
    }

    @Override
    public long count() {
        throw new NotImplementedException();
    }

    @Override
    public void deleteById(Long aLong) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(M2MClient entity) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAll(Iterable<? extends M2MClient> entities) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAll() {
        throw new NotImplementedException();
    }

    @Override
    public List<M2MClient> findAll(Sort sort) {
        throw new NotImplementedException();
    }

    @Override
    public Page<M2MClient> findAll(Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> Optional<S> findOne(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> long count(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient> boolean exists(Example<S> example) {
        throw new NotImplementedException();
    }

    @Override
    public <S extends M2MClient, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new NotImplementedException();
    }
}
