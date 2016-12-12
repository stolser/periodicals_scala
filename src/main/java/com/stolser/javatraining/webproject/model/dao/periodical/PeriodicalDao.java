package com.stolser.javatraining.webproject.model.dao.periodical;

import com.stolser.javatraining.webproject.model.dao.GenericDao;
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical;

import java.util.List;

public interface PeriodicalDao extends GenericDao<Periodical> {
    Periodical findOneByName(String name);
    List<Periodical> findAllByStatus(Periodical.Status status);
    void deleteAllDiscarded();
}
