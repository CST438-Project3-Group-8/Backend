package com.studyhive.spring_boot_docker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CourseDataLoader implements CommandLineRunner {

    private final CourseRepository courseRepository;

    public CourseDataLoader(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        if (courseRepository.count() == 0) {
            courseRepository.save(new Course("CST 201", "Media Tools I", "CST"));
            courseRepository.save(new Course("CST 202", "Drawing for Digital Media", "CST"));
            courseRepository.save(new Course("CST 202L", "Drawing for Digital Media Lab", "CST"));
            courseRepository.save(new Course("CST 205", "Multimedia Design and Programming", "CST"));
            courseRepository.save(new Course("CST 226", "Digital Photography", "CST"));
            courseRepository.save(new Course("CST 227", "Design Fundamentals", "CST"));
            courseRepository.save(new Course("CST 230", "Media Tools II", "CST"));
            courseRepository.save(new Course("CST 231", "Problem-Solving/Programming", "CST"));
            courseRepository.save(new Course("CST 237", "Intro to Computer Architecture", "CST"));
            courseRepository.save(new Course("CST 238", "Introduction to Data Structures", "CST"));
            courseRepository.save(new Course("CST 251", "Web Tools", "CST"));
            courseRepository.save(new Course("CST 271", "Digital Culture", "CST"));
            courseRepository.save(new Course("CST 274", "History of Communication Technologies and Politics in America", "CST"));
            courseRepository.save(new Course("CST 300", "Graduation Writing Assessment for Computing and Design", "CST"));
            courseRepository.save(new Course("CST 302", "History of Communication Design", "CST"));
            courseRepository.save(new Course("CST 304", "Typography", "CST"));
            courseRepository.save(new Course("CST 311", "Introduction to Computer Networks", "CST"));
            courseRepository.save(new Course("CST 315", "Introduction to Cybersecurity", "CST"));
            courseRepository.save(new Course("CST 316", "Computing for Designers", "CST"));
            courseRepository.save(new Course("CST 321", "Game Design and Interactive Media I", "CST"));
            courseRepository.save(new Course("CST 325", "Graphics Programming", "CST"));
            courseRepository.save(new Course("CST 326", "Game Development", "CST"));
            courseRepository.save(new Course("CST 327", "Experimental Typography", "CST"));
            courseRepository.save(new Course("CST 328", "Digital Art and Design", "CST"));
            courseRepository.save(new Course("CST 329", "Reasoning with Logic", "CST"));
            courseRepository.save(new Course("CST 334", "Operating Systems", "CST"));
            courseRepository.save(new Course("CST 336", "Internet Programming", "CST"));
            courseRepository.save(new Course("CST 338", "Software Design", "CST"));
            courseRepository.save(new Course("CST 345", "Visual Thinking", "CST"));
            courseRepository.save(new Course("CST 346", "Human-Computer Interaction", "CST"));
            courseRepository.save(new Course("CST 349", "Computer Science Proseminar", "CST"));
            courseRepository.save(new Course("CST 350", "Web Scripting", "CST"));
            courseRepository.save(new Course("CST 363", "Introduction to Database Systems", "CST"));
            courseRepository.save(new Course("CST 370", "Design and Analysis of Algorithms", "CST"));
            courseRepository.save(new Course("CST 380", "Mobile and Ubiquitous Computing", "CST"));
            courseRepository.save(new Course("CST 383", "Introduction to Data Science", "CST"));
            courseRepository.save(new Course("CST 395", "Special Topics", "CST"));
            courseRepository.save(new Course("CST 397", "Independent Study", "CST"));
            courseRepository.save(new Course("CST 404", "Publication Design", "CST"));
            courseRepository.save(new Course("CST 422", "Level Design", "CST"));
            courseRepository.save(new Course("CST 423", "Character Animation", "CST"));
            courseRepository.save(new Course("CST 438", "Software Engineering", "CST"));
            courseRepository.save(new Course("CST 446", "User Research and Experience Design", "CST"));
            courseRepository.save(new Course("CST 462S", "Race, Gender, Class in the Digital World", "CST"));
            courseRepository.save(new Course("CST 498", "Communication Design Capstone", "CST"));
            courseRepository.save(new Course("CST 499", "Computer Science Capstone", "CST"));
        }
    }
}