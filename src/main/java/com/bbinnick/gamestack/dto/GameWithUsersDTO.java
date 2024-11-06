package com.bbinnick.gamestack.dto;

import java.util.List;
import com.bbinnick.gamestack.model.User;
import lombok.Data;

@Data
public class GameWithUsersDTO {
	private Long id;
	private String title;
	private String genre;
	private String platform;
	private String imageUrl;
	private List<User> backlogUsers;
}
