package sw.study.user.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    String oldPassword;
    String newPassword;
}
