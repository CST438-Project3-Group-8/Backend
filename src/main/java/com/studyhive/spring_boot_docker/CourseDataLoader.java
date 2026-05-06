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
        saveIfMissing("CST 201", "Media Tools I", "CST");
        saveIfMissing("CST 202", "Drawing for Digital Media", "CST");
        saveIfMissing("CST 202L", "Drawing for Digital Media Lab", "CST");
        saveIfMissing("CST 205", "Multimedia Design and Programming", "CST");
        saveIfMissing("CST 226", "Digital Photography", "CST");
        saveIfMissing("CST 227", "Design Fundamentals", "CST");
        saveIfMissing("CST 230", "Media Tools II", "CST");
        saveIfMissing("CST 231", "Problem-Solving/Programming", "CST");
        saveIfMissing("CST 237", "Intro to Computer Architecture", "CST");
        saveIfMissing("CST 238", "Introduction to Data Structures", "CST");
        saveIfMissing("CST 251", "Web Tools", "CST");
        saveIfMissing("CST 271", "Digital Culture", "CST");
        saveIfMissing("CST 274", "History of Communication Technologies and Politics in America", "CST");
        saveIfMissing("CST 300", "Graduation Writing Assessment for Computing and Design", "CST");
        saveIfMissing("CST 302", "History of Communication Design", "CST");
        saveIfMissing("CST 304", "Typography", "CST");
        saveIfMissing("CST 311", "Introduction to Computer Networks", "CST");
        saveIfMissing("CST 315", "Introduction to Cybersecurity", "CST");
        saveIfMissing("CST 316", "Computing for Designers", "CST");
        saveIfMissing("CST 321", "Game Design and Interactive Media I", "CST");
        saveIfMissing("CST 325", "Graphics Programming", "CST");
        saveIfMissing("CST 326", "Game Development", "CST");
        saveIfMissing("CST 327", "Experimental Typography", "CST");
        saveIfMissing("CST 328", "Digital Art and Design", "CST");
        saveIfMissing("CST 329", "Reasoning with Logic", "CST");
        saveIfMissing("CST 334", "Operating Systems", "CST");
        saveIfMissing("CST 336", "Internet Programming", "CST");
        saveIfMissing("CST 338", "Software Design", "CST");
        saveIfMissing("CST 345", "Visual Thinking", "CST");
        saveIfMissing("CST 346", "Human-Computer Interaction", "CST");
        saveIfMissing("CST 349", "Computer Science Proseminar", "CST");
        saveIfMissing("CST 350", "Web Scripting", "CST");
        saveIfMissing("CST 363", "Introduction to Database Systems", "CST");
        saveIfMissing("CST 370", "Design and Analysis of Algorithms", "CST");
        saveIfMissing("CST 380", "Mobile and Ubiquitous Computing", "CST");
        saveIfMissing("CST 383", "Introduction to Data Science", "CST");
        saveIfMissing("CST 395", "Special Topics", "CST");
        saveIfMissing("CST 397", "Independent Study", "CST");
        saveIfMissing("CST 404", "Publication Design", "CST");
        saveIfMissing("CST 422", "Level Design", "CST");
        saveIfMissing("CST 423", "Character Animation", "CST");
        saveIfMissing("CST 438", "Software Engineering", "CST");
        saveIfMissing("CST 446", "User Research and Experience Design", "CST");
        saveIfMissing("CST 462S", "Race, Gender, Class in the Digital World", "CST");
        saveIfMissing("CST 498", "Communication Design Capstone", "CST");
        saveIfMissing("CST 499", "Computer Science Capstone", "CST");
    }

    private void saveIfMissing(String code, String title, String subject) {
        if (!courseRepository.existsByCode(code)) {
            courseRepository.save(new Course(code, title, subject));
        }
    }
}