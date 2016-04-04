package org.jnode.rest.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import org.jnode.rest.core.BadJsonException;
import org.jnode.rest.core.Http;
import org.jnode.rest.core.StringUtils;
import org.jnode.rest.dto.EchoMessage;
import org.jnode.rest.dto.PostMessageResult;
import org.jnode.rest.mapper.EchoMessageMapper;
import org.jnode.rest.mapper.PostMessageResultMapper;
import spark.Request;
import spark.Response;
import spark.Route;

public class PostEchoareaRoute extends Route {

    public PostEchoareaRoute(String path) {
        super(path);
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");

        EchoMessage echoMessage;
        try {
            echoMessage = EchoMessageMapper.fromJson(request.body());
        } catch (BadJsonException e) {
            response.status(Http.BAD_REQUEST);
            try {
                return PostMessageResultMapper.toJson(PostMessageResult.bad(e.getMessage()));
            } catch (JsonProcessingException e1) {
                response.status(Http.INTERNAL_SERVER_ERROR);
                return StringUtils.EMPTY;
            }
        }

       Echoarea area = FtnTools.getAreaByName(echoMessage.getEchoArea(), null);
        if(area == null){
            try {
                return PostMessageResultMapper.toJson(PostMessageResult.bad(String.format("area %s not found", area)));
            } catch (JsonProcessingException e1) {
                response.status(Http.INTERNAL_SERVER_ERROR);
                return StringUtils.EMPTY;
            }
        }


        Long id = FtnTools.writeEchomail(area, echoMessage.getSubject(), echoMessage.getBody(), echoMessage.getFromName(), echoMessage.getToName());

        try {
            response.status(Http.CREATED);
            return PostMessageResultMapper.toJson(PostMessageResult.good(id));
        } catch (JsonProcessingException e) {
            response.status(Http.INTERNAL_SERVER_ERROR);
            return StringUtils.EMPTY;
        }
    }
}
