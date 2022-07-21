package ru.job4j.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.domain.Person;
import ru.job4j.chat.domain.Room;
import ru.job4j.chat.domain.RoomDTO;
import ru.job4j.chat.service.PersonService;
import ru.job4j.chat.service.RoomService;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * 3. Мидл
 * 3.4. Spring
 * 3.4.8. Rest
 * 2. Создания чата на Rest API. [#9143]
 * RoomController rest api модели role.
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 13.07.2022
 */
@RestController
@RequestMapping("/room")
public class RoomController {
    private static final Logger LOG = LoggerFactory.getLogger(RoomController.class.getSimpleName());
    private static final String API_PERSON_ID = "http://localhost:8080/person/{id}";
    private final PersonService persons;
    private final RestTemplate rest;
    private final RoomService rooms;

    public RoomController(RestTemplate rest, RoomService rooms,
                          PersonService persons) {
        this.rest = rest;
        this.rooms = rooms;
        this.persons = persons;
    }

    @GetMapping("/")
    public Iterable<Room> findAll() {
        LOG.info("Find all Room");
        Iterable<Room> result = this.rooms.findAll();
        for (Room room : result) {
            room.setAdmin(
                    this.rest.getForObject(
                            API_PERSON_ID,
                            Person.class,
                            room.getAdmin().getId()));
        }
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id) {
        LOG.info("Room find by id={}", id);
        Optional<Room> room = this.rooms.findById(id);
        room.ifPresent(r -> r.setAdmin(
                this.rest.getForObject(
                        API_PERSON_ID,
                        Person.class,
                        room.get().getAdmin().getId())));
        return new ResponseEntity<>(
                room.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room is not found. Please, check requisites."
                )),
                HttpStatus.OK
        );
    }

    /**
     * Создание новой записи через DTO
     *
     * @param roomDTO RoomDTO
     * @return ResponseEntity
     */
    @PostMapping("/")
    public ResponseEntity<RoomDTO> create(@RequestBody RoomDTO roomDTO) {
        LOG.info("Create room={}", roomDTO);
        if (roomDTO.getName() == null) {
            throw new NullPointerException("Invalid room name");
        }
        var admin = persons.findById(roomDTO.getAdminId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Room room = Room.of(roomDTO.getName(), admin);
        return new ResponseEntity<>(
                rooms.domainToDTO(rooms.save(room)),
                HttpStatus.CREATED
        );
    }

    /**
     * Обновление заполненных полей через рефлексию и DTO
     *
     * @param roomDTO RoomDTO
     * @return ResponseEntity
     * @throws InvocationTargetException exception
     * @throws IllegalAccessException    exception
     */
    @PatchMapping("/")
    public ResponseEntity<RoomDTO> updateRoom(@RequestBody RoomDTO roomDTO) throws InvocationTargetException, IllegalAccessException {
        Optional<Room> current = rooms.findById(roomDTO.getId());
        if (current.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        RoomDTO currentDTO = rooms.domainToDTO(current.get());
        currentDTO = rooms.pathUpdate(currentDTO, roomDTO);
        current = Optional.of(rooms.dtoToDomain(currentDTO));
        rooms.save(current.get());
        return new ResponseEntity<>(
                currentDTO,
                HttpStatus.OK
        );
    }

    /**
     * Обновление модели через DTO
     *
     * @param roomDTO RoomDTO
     * @return ResponseEntity
     */
    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody RoomDTO roomDTO) {
        LOG.info("Update room={}", roomDTO);
        if (roomDTO.getName() == null) {
            throw new NullPointerException("Invalid room name");
        }
        Room room = rooms.dtoToDomain(roomDTO);
        this.rooms.save(room);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        LOG.info("Delete room by id={}", id);
        Room room = new Room();
        room.setId(id);
        this.rooms.delete(room);
        return ResponseEntity.ok().build();
    }
}
