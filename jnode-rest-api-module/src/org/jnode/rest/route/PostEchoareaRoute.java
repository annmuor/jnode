package org.jnode.rest.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jnode.rest.core.BadJsonException;
import org.jnode.rest.core.Http;
import org.jnode.rest.core.StringUtils;
import org.jnode.rest.dto.Message;
import org.jnode.rest.dto.PostMessageResult;
import org.jnode.rest.mapper.MessageMapper;
import org.jnode.rest.mapper.PostMessageResultMapper;
import spark.Request;
import spark.Response;
import spark.Route;

public class PostEchoareaRoute extends Route {
    public PostEchoareaRoute() {
        super("/echoarea");
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");

        Message message;
        try {
            message = MessageMapper.fromJson(request.body());
        } catch (BadJsonException e) {
            response.status(Http.BAD_REQUEST);
            try {
                return PostMessageResultMapper.toJson(PostMessageResult.bad(e.getMessage()));
            } catch (JsonProcessingException e1) {
                response.status(Http.INTERNAL_SERVER_ERROR);
                return StringUtils.EMPTY;
            }
        }




        try {
            return PostMessageResultMapper.toJson(PostMessageResult.good(1L));
        } catch (JsonProcessingException e) {
            response.status(Http.INTERNAL_SERVER_ERROR);
            return StringUtils.EMPTY;
        }
    }
}
