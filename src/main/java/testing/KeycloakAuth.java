package testing;

import gh.giceratops.api.client.ApiFormData;
import gh.giceratops.jutil.Json;

import java.util.HashMap;
import java.util.Map;

public class KeycloakAuth {

    static record Req(String username, String password) implements ApiFormData {

        @Override
        public Map<String, String> formData() {
            final var data = new HashMap<String, String>();
            data.put("username", this.username);
            data.put("password", this.password);
            data.put("client_id", "syrup-site");
            data.put("grant_type", "password");
            return data;
        }
    }

    static class Resp {
        String access_token;
        int expires_in;
        int refresh_expires_in;
        String refresh_token;
        String token_type;
        String session_state;
        String scope;

        @Override
        public String toString() {
            return Json.stringify(this, true);
        }
    }

    static class UserInfo {
        String sub;
        boolean email_verified;
        String preferred_username;

        @Override
        public String toString() {
            return Json.stringify(this, true);
        }
    }
}
