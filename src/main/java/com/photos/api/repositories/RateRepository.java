package com.photos.api.repositories;

import com.photos.api.models.Photo;
import com.photos.api.models.Rate;
import com.photos.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Micha Królewski on 2018-04-21.
 * @version 1.0
 */

@Component
public interface RateRepository extends JpaRepository<Rate, Long> {

}
