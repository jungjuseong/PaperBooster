package com.paperlink.service;

import com.paperlink.domain.Pantone;
import com.paperlink.repository.PantoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PantoneService {
    @Autowired
    PantoneRepository pantoneRepository;

    public Pantone save(Pantone pantone) {
        return pantoneRepository.save(pantone);
    }

    public List<Pantone> findAll() {
        return pantoneRepository.findAll();
    }

	public Pantone findOne(String pantoneId) {
		return pantoneRepository.findOne(pantoneId);
	}
	
	public void delete(String pantoneId) {
		pantoneRepository.delete(pantoneId);
	}
}
