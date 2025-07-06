package authservice.event.producer.serializer;

import authservice.model.UserInfoDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserInfoSerializer implements Serializer<UserInfoDto> {
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		Serializer.super.configure(configs, isKey);
	}

	@Override
	public byte[] serialize(String s, UserInfoDto userInfoDto) {
		byte[] retVal = null;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		try {
			retVal = objectMapper.writeValueAsString(userInfoDto).getBytes();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return retVal;
	}
}

//{
//		"Value": "{\"username\":\"srivautk2\",\"password\":\"$2a$10$9Fxkmpd9SU1V.pGqBdI7J.LKzOOC5jXXAdCQ4lEBmV9hQA29xAlwe\",\"roles\":[],\"firstName\":\"user2\",\"lastName\":\"last2\",\"email\":\"user2@gmail.com\"}",
//		"Offset": 1,
//		"Key": null,
//		"Partition": 0,
//		"Headers": {},
//		"Timestamp": "2025-06-29T20:24:05.313Z"
//		}
