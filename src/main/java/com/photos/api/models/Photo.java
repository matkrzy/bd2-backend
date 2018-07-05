package com.photos.api.models;

import com.fasterxml.jackson.annotation.*;
import com.photos.api.resolvers.EntityIdResolver;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.photos.api.models.enums.PhotoState;
import com.photos.api.models.enums.PhotoVisibility;

/**
 * @author Micha Królewski on 2018-04-07.
 * @version 1.0
 */

@Entity
@Table(name = "photo")
@ApiModel
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        resolver = EntityIdResolver.class,
        scope = Photo.class
)
public class Photo {
    @Id
    @GeneratedValue
    @NotNull
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name")
    @ApiModelProperty(required = true)
    private String name;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", updatable = false)
    @ApiModelProperty(readOnly = true)
    private Date creationDate;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonProperty("userId")
    @JsonIdentityReference(alwaysAsId = true)
    @ApiModelProperty(required = true, dataType = "int")
    private User user;

    @Column(name = "path")
    private String path;

    @Column(name = "url")
    private String url;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", columnDefinition="enum('PUBLIC','PRIVATE')")
    @ApiModelProperty(allowableValues = "PUBLIC,PRIVATE")
    private PhotoVisibility visibility = PhotoVisibility.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", columnDefinition="enum('ARCHIVED','ACTIVE')")
    @ApiModelProperty(allowableValues = "ARCHIVED,ACTIVE")
    private PhotoState state = PhotoState.ACTIVE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "photo_to_tag",
            joinColumns = {@JoinColumn(name = "photo_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_name")}
    )
    @JsonProperty("tags")
    @JsonIdentityReference(alwaysAsId = true)
    @ApiModelProperty(dataType = "[Ljava.lang.String")
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "photo_to_category",
            joinColumns = {@JoinColumn(name = "photo_id")},
            inverseJoinColumns = {@JoinColumn(name = "category_id")}
    )
    @JsonProperty("categoryIds")
    @JsonIdentityReference(alwaysAsId = true)
    @ApiModelProperty(dataType = "[I")
    private Set<Category> categories = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "photo")
    @JsonProperty("shareIds")
    @JsonIdentityReference(alwaysAsId = true)
    @ApiModelProperty(dataType = "[I")
    private Set<Share> shares = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "photo")
    @JsonProperty("likeIds")
    @JsonIdentityReference(alwaysAsId = true)
    @ApiModelProperty(dataType = "[I")
    private Set<Like> likes = new HashSet<>();

    public Photo() {
    }

    public Photo(
            @NotNull String name,
            @NotNull User user,
            String path,
            String description,
            PhotoVisibility visibility,
            PhotoState state
    ) {
        this.name = name;
        this.user = user;
        this.path = path;
        this.description = description;
        this.visibility = visibility;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PhotoVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PhotoVisibility visibility) {
        this.visibility = visibility;
    }

    public PhotoState getState() {
        return state;
    }

    public void setState(PhotoState state) {
        this.state = state;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Set<Share> getShares() {
        return shares;
    }

    public void setShares(Set<Share> shares) {
        this.shares = shares;
    }

    public Set<Like> getLikes() {
        return likes;
    }

    public void setLikes(Set<Like> likes) {
        this.likes = likes;
    }
}
