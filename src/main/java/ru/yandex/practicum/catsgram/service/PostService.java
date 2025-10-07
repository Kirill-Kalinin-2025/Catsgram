package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(Integer from, Integer size, String sort) {
        if (size <= 0) {
            throw new ConditionsNotMetException("Размер страницы должен быть больше нуля");
        }

        if (from < 0) {
            throw new ConditionsNotMetException("Начальная позиция должна быть больше нуля");
        }

        Collection<Post> sortedPosts = getSortedPosts(sort);

        return sortedPosts.stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    private List<Post> getSortedPosts(String sort) {
        List<Post> allPosts = new ArrayList<>(posts.values());

        if ("asc".equalsIgnoreCase(sort)) {
            // По возрастанию (старые сначала)
            allPosts.sort(Comparator.comparing(Post::getPostDate));
        } else if ("desc".equalsIgnoreCase(sort)) {
            // По убыванию (новые сначала)
            allPosts.sort(Comparator.comparing(Post::getPostDate).reversed());
        } else {
            throw new ConditionsNotMetException("Параметр sort может быть только 'asc' или 'desc'");
        }

        return allPosts;
    }

    public Optional<Post> findPostById(Long id) {
        return Optional.ofNullable(posts.get(id));
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }

        if (post.getAuthorId() == null) {
            throw new ConditionsNotMetException("ID автора должен быть указан");
        }

        userService.findUserById(post.getAuthorId())
                .orElseThrow(() -> new ConditionsNotMetException(
                        "Автор с id = " + post.getAuthorId() + " не найден"
                ));

        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(posts.get(id));
    }
}