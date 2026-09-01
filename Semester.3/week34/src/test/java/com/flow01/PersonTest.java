package com.flow01;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PersonTest {

    @Test
    void defaultConstructorCreatesEmptyPerson() {
        Person person = new Person();

        assertNull(person.getFirstName());
        assertNull(person.getLastName());
        assertEquals(0, person.getAge());
    }

    @Test
    void allArgsConstructorSetsAllFields() {
        Person person = new Person("John", "Doe", 25);

        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
        assertEquals(25, person.getAge());
    }

    @Test
    void settersUpdateFields() {
        Person person = new Person();

        person.setFirstName("Jane");
        person.setLastName("Roe");
        person.setAge(31);

        assertEquals("Jane", person.getFirstName());
        assertEquals("Roe", person.getLastName());
        assertEquals(31, person.getAge());
    }

    @Test
    void toStringContainsAllFieldValues() {
        Person person = new Person("John", "Doe", 26);

        assertEquals("Person(firstName=John, lastName=Doe, age=26)", person.toString());
    }
}
