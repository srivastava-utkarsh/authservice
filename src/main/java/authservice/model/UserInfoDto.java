package authservice.model;

import authservice.entities.UserInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoDto extends UserInfo
{

	@NonNull
	private String firstName;

	@NonNull
	private String lastName;


	private Long phoneNumber;

	private String email;

}
