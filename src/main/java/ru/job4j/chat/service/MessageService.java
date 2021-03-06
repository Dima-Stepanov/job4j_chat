package ru.job4j.chat.service;

import liquibase.pro.packaged.M;
import org.springframework.stereotype.Service;
import ru.job4j.chat.domain.Message;
import ru.job4j.chat.domain.MessageDTO;
import ru.job4j.chat.domain.Person;
import ru.job4j.chat.domain.Room;
import ru.job4j.chat.repository.MessageRepository;

import java.util.List;
import java.util.Optional;

/**
 * 3. Мидл
 * 3.4. Spring
 * 3.4.8. Rest
 * 2. Создания чата на Rest API. [#9143]
 * MessageService слой бизнес логики модели message.
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 13.07.2022
 */
@Service
public class MessageService implements IService<Message, MessageDTO> {
    private final MessageRepository messages;

    public MessageService(MessageRepository messages) {
        this.messages = messages;
    }

    @Override
    public Message save(Message type) {
        return this.messages.save(type);
    }

    @Override
    public Optional<Message> findById(int id) {
        return this.messages.findById(id);
    }

    @Override
    public void delete(Message type) {
        this.messages.delete(type);
    }

    @Override
    public Iterable<Message> findAll() {
        return this.messages.findAll();
    }

    public List<Message> fidAllByRoom(Room room) {
        return this.messages.findAllByRoom(room);
    }

    public List<Message> findAllByPerson(Person person) {
        return this.messages.findAllByPerson(person);
    }


    @Override
    public Message dtoToDomain(MessageDTO dto) {
        Person person = new Person();
        person.setId(dto.getPersonId());
        Room room = new Room();
        room.setId(dto.getRoomId());
        Message message = Message.of(dto.getText(), person, room);
        message.setId(dto.getId());
        return message;
    }

    @Override
    public MessageDTO domainToDTO(Message domain) {
        return MessageDTO.of(domain.getId(), domain.getText(),
                domain.getPerson().getId(), domain.getRoom().getId());
    }
}
