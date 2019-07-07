package com.intellif.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author inori
 * @create 2019-07-02 13:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String name;

    private String sex;

    private int age;

}