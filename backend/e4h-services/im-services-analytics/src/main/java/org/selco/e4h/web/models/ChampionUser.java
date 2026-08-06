package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A user ranked among the most active within a role or an application for the reported week.
 * <p>
 * The ranking counts every event <em>except</em> {@code USER_LOGIN} — a champion is someone who did
 * work in the week, and simply signing in repeatedly is not work.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChampionUser {

    /** The user's uuid, which the ranking is grouped on. */
    private String uuid;

    /** Login id, typically the mobile number. */
    private String userName;

    /** Display name. */
    private String name;

    /** Events attributed to this user in the week, logins excluded. */
    private long activityCount;
}
