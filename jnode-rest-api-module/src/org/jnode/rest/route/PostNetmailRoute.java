package org.jnode.rest.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import org.jnode.rest.core.BadJsonException;
import org.jnode.rest.core.Http;
import org.jnode.rest.core.StringUtils;
import org.jnode.rest.dto.NetmailMessage;
import org.jnode.rest.dto.PostMessageResult;
import org.jnode.rest.mapper.NetmailMessageMapper;
import org.jnode.rest.mapper.PostMessageResultMapper;
import spark.Request;
import spark.Response;
import spark.Route;

public class PostNetmailRoute extends Route {
    public PostNetmailRoute(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");

        NetmailMessage msg;
        try {
            msg = NetmailMessageMapper.fromJson(request.body());
        } catch (BadJsonException e) {
            response.status(Http.BAD_REQUEST);
            try {
                return PostMessageResultMapper.toJson(PostMessageResult.bad(e.getMessage()));
            } catch (JsonProcessingException e1) {
                response.status(Http.INTERNAL_SERVER_ERROR);
                return StringUtils.EMPTY;
            }
        }


        FtnAddress from;
        try{
            from = new FtnAddress(msg.getFromFtnAddr());
        } catch(NumberFormatException e){
            response.status(Http.BAD_REQUEST);
            try {
                return PostMessageResultMapper.toJson(PostMessageResult.bad("bad from ftn: " + msg.getFromFtnAddr()));
            } catch (JsonProcessingException e1) {
                response.status(Http.INTERNAL_SERVER_ERROR);
                return StringUtils.EMPTY;
            }
        }

        FtnAddress to;
        try{
            to = new FtnAddress(msg.getToFtnAddr());
        } catch(NumberFormatException e){
            response.status(Http.BAD_REQUEST);
            try {
                return PostMessageResultMapper.toJson(PostMessageResult.bad("bad to ftn: " + msg.getToFtnAddr()));
            } catch (JsonProcessingException e1) {
                response.status(Http.INTERNAL_SERVER_ERROR);
                return StringUtils.EMPTY;
            }
        }

        Long id = FtnTools.writeNetmail(from, to, msg.getFromName(), msg.getToName(), msg.getSubject(), msg.getBody());

        try {
            response.status(Http.CREATED);
            return PostMessageResultMapper.toJson(PostMessageResult.good(id));
        } catch (JsonProcessingException e) {
            response.status(Http.INTERNAL_SERVER_ERROR);
            return StringUtils.EMPTY;
        }
    }
}
