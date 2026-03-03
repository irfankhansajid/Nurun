package com.nurun.router;

import com.nurun.dto.AiRequest;
import com.nurun.dto.AiResponse;

public interface AiRouter {
        AiResponse generate(AiRequest request);

}
