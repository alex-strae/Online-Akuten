package com.portfolio.OnlineAkuten.controller;

import java.util.*;

import com.portfolio.OnlineAkuten.model.Pathosis;
import com.portfolio.OnlineAkuten.model.Patient;
import com.portfolio.OnlineAkuten.model.Person;
import com.portfolio.OnlineAkuten.repositories.PathosisRepository;
import com.portfolio.OnlineAkuten.repositories.PatientRepository;
import com.portfolio.OnlineAkuten.repositories.PersonRepository;
import com.portfolio.OnlineAkuten.model.*;
import com.portfolio.OnlineAkuten.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.*;

@CrossOrigin
@RestController
@RequestMapping("/")
public class OnlineAkutenController {
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PathosisRepository pathosisRepository;


    @GetMapping("/persons")
    public List<Person> getAllPersons() {
        return this.personRepository.findAll();
    }

    @GetMapping("/persons/byAge")
    public List<Person> getAllPersonsByAge()
    {return this.personRepository.findAllByOrderByAgeDesc();}

    @GetMapping("/persons/byAge/{start}-{end}")
    public List<Person> getAllPersonsByAgeBetween(@PathVariable("start") Integer start, @PathVariable("end") Integer end)
        {return this.personRepository.findAllByAgeBetweenOrderByAgeAsc(start, end);}

    @GetMapping("/persons/byPostal")
    public List<Person> getAllPersonsByPostal()
    {return this.personRepository.findAllByOrderByPostalAsc();}

    @PostMapping("/persons")
    @ResponseStatus(HttpStatus.CREATED)
    public Person addPerson(@RequestBody Person person) {
        person.setIdNull();
        return this.personRepository.save(person);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/persons/{id}")
    public Person updatePerson(@PathVariable("id") Integer id, @RequestBody Person person){
        Optional<Person> personToUpdateOptional = this.personRepository.findById(id);
        if (personToUpdateOptional.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Person personToUpdate = personToUpdateOptional.get();
        if (person.getLastName() != null){
            personToUpdate.setLastName(person.getLastName());
        }
        if (person.getFirstName() != null) {
            personToUpdate.setFirstName(person.getFirstName());
        }
        if (person.getAge() != null) {
            personToUpdate.setAge(person.getAge());
        }
        if (person.getPostal() != null){
            personToUpdate.setPostal(person.getPostal());
        }
        return this.personRepository.save(personToUpdate);
    }
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/persons/{id}")
    public Person deletePerson(@PathVariable("id") Integer id){
        Optional<Person> personToDeleteOptional = this.personRepository.findById(id);
        if (personToDeleteOptional.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        this.personRepository.delete(personToDeleteOptional.get());
        return personToDeleteOptional.get();
    }

    @GetMapping("/patients")
    public List<Patient> createAndGetAllPatients() {
        this.patientRepository.deleteAll();
        List<Person> listOfPersons = this.personRepository.findAll();
        List<Pathosis> listOfPathosis = this.pathosisRepository.findAll();

        for (Person person:listOfPersons) {
            int dice = (int)(Math.random()*(listOfPathosis.size()));
            Pathosis sickness = listOfPathosis.get(dice);
            Patient newPatient = new Patient(person.getId(), sickness.getId());
            this.patientRepository.save(newPatient);
        }
        return this.patientRepository.findAll();
    }


    @GetMapping("/pathosis")
    public List<Pathosis> getAllPathosis() {
        return this.pathosisRepository.findAll();
    }

    @GetMapping("/pathosis/{id}")
    public Pathosis getPathosisById(@PathVariable("id") Integer id) {
        Optional<Pathosis> pathosisOptional = this.pathosisRepository.findById(id);
        if (pathosisOptional.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Pathosis pathosis = pathosisOptional.get();
        return pathosis;
    }

    @PostMapping("/pathosis")
    public Pathosis addPathosis(@RequestBody Pathosis pathosis){
        pathosis.setIdNull();
        return this.pathosisRepository.save(pathosis);
    }
    @Secured("ROLE_ADMIN")
    @PutMapping("/pathosis/{id}")
    public Pathosis updatePathosis(@PathVariable("id") Integer id, @RequestBody Pathosis p){
        Optional<Pathosis> pathosisToUpdateOptional = this.pathosisRepository.findById(id);
        if (pathosisToUpdateOptional.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Pathosis pathosisToUpdate = pathosisToUpdateOptional.get();
        if (p.getName() != null){
            pathosisToUpdate.setName(p.getName());
        }
        if (p.getMortality() != null) {
            pathosisToUpdate.setMortality(p.getMortality());
        }
        if (p.getSymptomOne() != null) {
            pathosisToUpdate.setSymptomOne(p.getSymptomOne());
        }
        if (p.getSymptomTwo() != null){
            pathosisToUpdate.setSymptomTwo(p.getSymptomTwo());
        }
        if (p.getSymptomThree() != null){
            pathosisToUpdate.setSymptomThree(p.getSymptomThree());
        }
        return this.pathosisRepository.save(pathosisToUpdate);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/pathosis/{id}")
    public Pathosis deletePathosisById(@PathVariable("id") Integer id) {
        Optional<Pathosis> pathosisToDeleteOptional = this.pathosisRepository.findById(id);
        if (pathosisToDeleteOptional.isEmpty()) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }

        this.pathosisRepository.deleteById(id);

        return pathosisToDeleteOptional.get();
    }

    @PostMapping("/sendToSurgery/{type}/{timer}")
    public List<Patient> sendToSurgery(@PathVariable("type") String waitingRoom, @PathVariable("timer") Integer timer, @RequestBody List<Patient> patients){
        if (!waitingRoom.equals("q") && !waitingRoom.equals("s")){ throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
        int max_mortality = 5;
        Queue<Patient> q = new LinkedList<>();
        Stack<Patient> s = new Stack<>();
        for (int i = 0 ; i < 5; i++) {
            for (Patient patient : patients) {
                Optional<Pathosis> currentDiseaseOptional = this.pathosisRepository.findById(patient.getPathosisId());
                Pathosis currentDisease = currentDiseaseOptional.get();
                if (currentDisease.getMortality() == max_mortality) {
                    if (waitingRoom.equals("q")) { q.add(patient); }
                    else if (waitingRoom.equals("s")) { s.push(patient); }
                }
            }
            max_mortality = max_mortality - 1;
        }
        Patient luckyPatient = null;
        for (int i = 0; i < timer && i < patients.size(); i ++){
            if (waitingRoom.equals("q")) { luckyPatient = q.remove(); }
            else if (waitingRoom.equals("s")) { luckyPatient = s.pop();}

            Optional<Pathosis> formerDiseaseOptional = this.pathosisRepository.findById(luckyPatient.getPathosisId());
            Pathosis formerDisease = formerDiseaseOptional.get();
            Integer timeTaken = formerDisease.getMortality() + (int)(Math.random()*(10));
            timer = timer - timeTaken;
            luckyPatient.setCured(true);
        }
        return getNamesFromIds(patients);
    }

    public List<Patient> getNamesFromIds(List<Patient> patientsAftermath){
        for (Patient patient : patientsAftermath){
            Optional<Pathosis> diseaseOptional = this.pathosisRepository.findById(patient.getPathosisId());
            Pathosis disease = diseaseOptional.get();
            patient.setPathosisName(disease.getName());
            Optional<Person> personOptional = this.personRepository.findById(patient.getPersonId());
            Person person = personOptional.get();
            patient.setFullName(person.getFirstName() + " " + person.getLastName());
        }
        return patientsAftermath;
    }

}


