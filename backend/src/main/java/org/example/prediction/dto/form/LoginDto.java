package org.example.prediction.dto.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto implements java.io.Serializable {
    private String username;
    private String password;
}