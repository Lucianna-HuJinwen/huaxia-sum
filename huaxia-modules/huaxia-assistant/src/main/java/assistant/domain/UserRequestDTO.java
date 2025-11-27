package assistant.domain;

import lombok.Data;


@Data
public class UserRequestDTO {

    private Long userId;

    private String message;
}
