package assistant.controller;

import assistant.domain.UserRequestDTO;
import com.liam.common.core.domain.R;
import com.liam.langchain4j.service.DeepSeekAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/assistant")
@CrossOrigin(origins = "*") // 临时允许所有前端访问
public class AssistantController {

    @Autowired
    private DeepSeekAssistant deepSeekAssistant;

    @PostMapping("/chat")
    public R<String> chat(@RequestBody UserRequestDTO userRequestDTO) {
        // 获取流式响应的第一个完整结果
        String response = deepSeekAssistant.chatWithChatMemory(userRequestDTO.getUserId(), userRequestDTO.getMessage())
                .collectList()
                .block()
                .stream()
                .reduce("", String::concat);
        return R.ok(response);
    }
    
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> chatStream(@RequestBody UserRequestDTO userRequestDTO) {
        return deepSeekAssistant.chatWithChatMemory(userRequestDTO.getUserId(), userRequestDTO.getMessage());
    }
}
