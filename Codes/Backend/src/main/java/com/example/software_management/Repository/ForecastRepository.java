package com.example.software_management.Repository;

import com.example.software_management.Model.Data;
import com.example.software_management.Model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Integer> {



}