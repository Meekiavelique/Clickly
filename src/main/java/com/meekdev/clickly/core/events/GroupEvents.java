package com.meekdev.clickly.core.events;

import com.meekdev.clickly.core.models.Group;
import com.meekdev.clickly.core.models.GroupMember;

public class GroupEvents {

    public static class GroupCreated extends BaseEvent {
        private final Group group;

        public GroupCreated(Group group) {
            this.group = group;
        }

        public Group getGroup() {
            return group;
        }
    }

    public static class GroupJoined extends BaseEvent {
        private final String groupId;
        private final GroupMember member;

        public GroupJoined(String groupId, GroupMember member) {
            this.groupId = groupId;
            this.member = member;
        }

        public String getGroupId() {
            return groupId;
        }

        public GroupMember getMember() {
            return member;
        }
    }

    public static class GroupLeft extends BaseEvent {
        private final String groupId;
        private final GroupMember member;

        public GroupLeft(String groupId, GroupMember member) {
            this.groupId = groupId;
            this.member = member;
        }

        public String getGroupId() {
            return groupId;
        }

        public GroupMember getMember() {
            return member;
        }
    }

    public static class GroupDeleted extends BaseEvent {
        private final String groupId;

        public GroupDeleted(String groupId) {
            this.groupId = groupId;
        }

        public String getGroupId() {
            return groupId;
        }
    }

    public static class MemberStatusUpdated extends BaseEvent {
        private final String groupId;
        private final GroupMember member;

        public MemberStatusUpdated(String groupId, GroupMember member) {
            this.groupId = groupId;
            this.member = member;
        }

        public String getGroupId() {
            return groupId;
        }

        public GroupMember getMember() {
            return member;
        }
    }
}