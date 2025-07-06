package authservice.event.producer;

import authservice.model.UserInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserInfoProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	@Value("${spring.kafka.topic.name}")
	private String TOPIC_NAME;

	@Autowired
	public UserInfoProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendKafkaEvent(UserInfoDto userInfoDto) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(userInfoDto);
		kafkaTemplate.send(TOPIC_NAME, json);
	}
}
