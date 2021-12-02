package com.example.projectapplication;

import java.util.ArrayList;

public class Group {
    private String id, hostId, name, description, imageUrl;
    private ArrayList<User> members;
    private ArrayList<String> searchTags;

    public Group() {

    }

    public Group (String id, String hostId, String name, String description, String imageUrl, ArrayList<User> members, ArrayList<String> searchTags) {
        this.id = id;
        this.hostId = hostId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.members = members;
        this.searchTags = searchTags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ArrayList<User> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<User> members) {
        this.members = members;
    }

    public ArrayList<String> getSearchTags() {
        return searchTags;
    }

    public void setSearchTags(ArrayList<String> searchTags) {
        this.searchTags = searchTags;
    }
}
