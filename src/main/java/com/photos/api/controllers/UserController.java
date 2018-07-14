package com.photos.api.controllers;

import com.photos.api.exceptions.*;
import com.photos.api.models.*;
import com.photos.api.models.dtos.FetchedCategory;
import com.photos.api.models.dtos.FetchedPhoto;
import com.photos.api.services.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;

/**
 * @author Micha Królewski on 2018-04-07.
 * @version 1.0
 */

@RestController
@RequestMapping("/users")
@Api(value = "User resource")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ReportService reportService;

    @ApiOperation(value = "Returns users", produces = "application/json", response = User.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Users retrieved successfully")
    })
    @GetMapping
    public ResponseEntity getUsers() {
        try {
            List<User> users = userService.getAll();

            return ResponseEntity.status(HttpStatus.OK).body(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Creates user", produces = "application/json", response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User created successfully"),
            @ApiResponse(code = 400, message = "Invalid entity given")
    })
    @PostMapping
    public ResponseEntity addUser(@RequestBody final User user) {
        try {
            User addedUser = userService.add(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(addedUser);
        } catch (UserEmailEmptyException | UserPasswordEmptyException | UserLastNameEmptyException | UserFirstNameEmptyException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns current user", produces = "application/json", response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User retrieved successfully")
    })
    @GetMapping("/me")
    public ResponseEntity getCurrent() {
        try {
            User user = userService.getCurrent();

            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns user by ID", produces = "application/json", response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User retrieved successfully"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @GetMapping("/{id}")
    public ResponseEntity getUser(@PathVariable final Long id) {
        try {
            User user = userService.getById(id);

            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Updates user", produces = "application/json", response = User.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User updated successfully"),
            @ApiResponse(code = 400, message = "Invalid entity given"),
            @ApiResponse(code = 403, message = "No permission to update given user"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @PutMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable final Long id, @RequestBody final User user) {
        if (!id.equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(user);
        }

        try {
            User updatedUser = userService.update(user);

            return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (EntityUpdateDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (UserEmailChangeDeniedException | UserPasswordEmptyException | UserLastNameEmptyException | UserFirstNameEmptyException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Deletes user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User deleted successfully"),
            @ApiResponse(code = 403, message = "No permission to delete given user"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable final Long id) {
        try {
            userService.delete(id);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (EntityDeleteDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns user photos by user ID", produces = "application/json", response = FetchedPhoto.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Photos retrieved successfully"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @GetMapping("/{id}/photos")
    public ResponseEntity getUserPhotos(
            @PathVariable final Long id,
            Pageable pageable
    ) {
        try {
            List<FetchedPhoto> photos = photoService.getAllActiveByUser(userService.getById(id), pageable);

            return ResponseEntity.status(HttpStatus.OK).body(photos);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns user archived photos by user ID", produces = "application/json", response = FetchedPhoto.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Photos retrieved successfully"),
            @ApiResponse(code = 403, message = "No permission to retrieve given user's archived photos"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @GetMapping("/{id}/photos/archived")
    public ResponseEntity getUserArchivedPhotos(
            @PathVariable final Long id,
            Pageable pageable
    ) {
        try {
            List<FetchedPhoto> photos = photoService.getAllArchivedByUser(userService.getById(id), pageable);

            return ResponseEntity.status(HttpStatus.OK).body(photos);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (EntityGetDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns user categories by user ID", produces = "application/json", response = FetchedCategory.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Categories retrieved successfully"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @GetMapping("/{id}/categories")
    public ResponseEntity getUserCategories(@PathVariable final Long id) {
        try {
            List<FetchedCategory> categories = categoryService.getAllByUser(userService.getById(id));

            return ResponseEntity.status(HttpStatus.OK).body(categories);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns user likes by user ID", produces = "application/json", response = Like.class, responseContainer = "Set")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Likes retrieved successfully"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @GetMapping("/{id}/likes")
    public ResponseEntity getUserLikes(@PathVariable final Long id) {
        try {
            Set<Like> likes = userService.getById(id).getLikes();

            return ResponseEntity.status(HttpStatus.OK).body(likes);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns user shares by user ID", produces = "application/json", response = Share.class, responseContainer = "Set")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Shares retrieved successfully"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @GetMapping("/{id}/shares")
    public ResponseEntity getUserShares(@PathVariable final Long id) {
        try {
            Set<Share> shares = userService.getById(id).getShares();

            return ResponseEntity.status(HttpStatus.OK).body(shares);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns user tags by user ID", produces = "application/json", response = Tag.class, responseContainer = "Set")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tags retrieved successfully"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @GetMapping("/{id}/tags")
    public ResponseEntity getUserTags(@PathVariable final Long id, @RequestParam(required = false) String q) {
        try {
            User user = userService.getById(id);
            Set<Tag> tags;

            if (q == null) {
                tags = tagService.getAllByUser(user);
            } else {
                tags = tagService.getAllByUserAndStartingWith(user, q);
            }

            return ResponseEntity.status(HttpStatus.OK).body(tags);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value = "Returns user report by user ID", produces = "application/pdf")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Report retrieved successfully"),
            @ApiResponse(code = 403, message = "No permission to retrieve given user's report"),
            @ApiResponse(code = 404, message = "User with given ID doesn't exist")
    })
    @GetMapping("/{id}/report")
    public ResponseEntity getUserPhotoReport(@PathVariable final Long id) {
        try {
            PDDocument document = reportService.getReportByUser(userService.getById(id));

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            document.close();

            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_PDF).body(byteArrayOutputStream.toByteArray());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (EntityGetDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
