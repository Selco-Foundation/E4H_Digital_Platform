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
 * <p>
 * The ranked entity is the user <em>and</em> the role they acted under, not the user alone: someone
 * who worked under two roles in the week is ranked once per role, on the events of that role.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChampionUser {

    /** The user's uuid, one half of what the ranking is grouped on. */
    private String uuid;

    /** The primary role the events were recorded under, the other half of the grouping. */
    private String role;

    /** Login id, typically the mobile number. */
    private String userName;

    /** Display name. */
    private String name;

    /** Events attributed to this user in this role in the week, logins excluded. */
    private long activityCount;
}
