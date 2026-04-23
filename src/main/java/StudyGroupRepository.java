import com.studyhive.spring_boot_docker.StudyGroup;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class StudyGroupRepository implements JpaRepository<StudyGroup, Long> {
    @Override
    public void flush() {

    }

    @Override
    public <S extends StudyGroup> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends StudyGroup> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<StudyGroup> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public StudyGroup getOne(Long aLong) {
        return null;
    }

    @Override
    public StudyGroup getById(Long aLong) {
        return null;
    }

    @Override
    public StudyGroup getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends StudyGroup> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends StudyGroup> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends StudyGroup> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends StudyGroup> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends StudyGroup> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends StudyGroup> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends StudyGroup, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends StudyGroup> S save(S entity) {
        return null;
    }

    @Override
    public <S extends StudyGroup> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<StudyGroup> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public List<StudyGroup> findAll() {
        return List.of();
    }

    @Override
    public List<StudyGroup> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(StudyGroup entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends StudyGroup> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<StudyGroup> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<StudyGroup> findAll(Pageable pageable) {
        return null;
    }
}
