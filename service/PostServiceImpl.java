package com.cts.cj.service;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cts.cj.constants.CJCommonConstants;
import com.cts.cj.domain.Comment;
import com.cts.cj.domain.CommentUrl;
import com.cts.cj.domain.Post;
import com.cts.cj.domain.PostUrl;
import com.cts.cj.domain.User;
import com.cts.cj.dto.CommentDto;
import com.cts.cj.dto.PostDto;
import com.cts.cj.repository.CommentRepository;
import com.cts.cj.repository.Neo4jQueries;
import com.cts.cj.repository.PostRepository;
import com.cts.cj.util.CJUtilCommon;
import com.cts.cj.util.FileStorageProperties;

@Repository
@Service

public class PostServiceImpl implements PostService {

	private final Path fileStorageLocation;

	@Autowired
	private SessionFactory session;
	@Autowired
	private PostRepository postRepo;
	@Autowired
	private CommentRepository commentRepo;
	@Autowired
	CJUtilCommon i2iCommonUtil;

	@Autowired
	public PostServiceImpl(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new com.cts.cj.exception.FileStorageException(
					"Could not create the directory where the uploaded files will be stored.", ex);
		}
	}

	@Override
	public List<PostDto> getPost(Long createdBy) {
		List<Post> postcreatedBy = new ArrayList<Post>();
		// String statement="match(a:Post) <- [b:POST_CREATED_BY] - (c:User) where
		// id(c)=$createdBy and a.isDeleted=false and a.isPrivate=true optional
		// match(a:Post) - [d:HAS_ASSOCIATED_URL] -> (e:PostUrl) return a,e,id(c)";
		Iterable<Map<String, Object>> dbPostList = session.openSession()
				.query(Neo4jQueries.STR_POST_CREATED_BY, Collections.singletonMap("createdBy", createdBy))
				.queryResults();
		dbPostList.forEach(r -> {
			Post dbPost = (Post) r.get("a");
			dbPost.setCreatedBy((Long) r.get("id(c)"));
			dbPost.setStrCreatedBy((String) r.get("c.first_name") + " " + (String) r.get("c.last_name"));
			dbPost.setProfileImageUrl((String)r.get("c.profileImageUrl"));
			List<PostUrl> listUrl = new ArrayList<PostUrl>();
			listUrl.add((PostUrl) r.get("e"));
			if (postcreatedBy.size() == 0) {
				dbPost.setUrlList(listUrl);
				postcreatedBy.add(dbPost);
			} else {
				boolean dataMatched = false;
				for (int i = 0; i < postcreatedBy.size(); i++) {
					Post p = postcreatedBy.get(i);
					if (p.getId().compareTo(dbPost.getId()) == 0) {
						List<PostUrl> urllist = p.getUrlList();
						if (urllist != null) {
							urllist.add((PostUrl) r.get("e"));
							p.setUrlList(urllist);
						}
						dataMatched = true;
					}
				}
				if (!dataMatched) {
					dbPost.setUrlList(listUrl);
					postcreatedBy.add(dbPost);
				}
			}

		});
		
		
				
		List<Post> posttaggedUser = new ArrayList<Post>();
		// String statement="match(a:Post) <- [b:POST_CREATED_BY] - (c:User) where
		// id(c)=$createdBy and a.isDeleted=false and a.isPrivate=true optional
		// match(a:Post) - [d:HAS_ASSOCIATED_URL] -> (e:PostUrl) return a,e,id(c)";
		Iterable<Map<String, Object>> dbPostList_tagUser = session.openSession()
				.query(Neo4jQueries.STR_POST_TAGGED_FOR, Collections.singletonMap("createdBy", createdBy))
				.queryResults();
		dbPostList_tagUser.forEach(r -> {
			Post dbPost = (Post) r.get("a");
			dbPost.setCreatedBy((Long) r.get("id(c)"));
			dbPost.setStrCreatedBy((String) r.get("c.first_name") + " " + (String) r.get("c.last_name"));
			dbPost.setProfileImageUrl((String)r.get("c.profileImageUrl"));
			List<PostUrl> listagUser = new ArrayList<PostUrl>();
			listagUser.add((PostUrl) r.get("e"));
			if (posttaggedUser.size() == 0) {
				dbPost.setUrlList(listagUser);
				posttaggedUser.add(dbPost);
			} else {
				boolean dataMatched = false;
				for (int i = 0; i < posttaggedUser.size(); i++) {
					Post p = posttaggedUser.get(i);
					if (p.getId().compareTo(dbPost.getId()) == 0) {
						List<PostUrl> urllist = p.getUrlList();
						if (urllist != null) {
							urllist.add((PostUrl) r.get("e"));
							p.setUrlList(urllist);
						}
						dataMatched = true;
					}
				}
				if (!dataMatched) {
					dbPost.setUrlList(listagUser);
					posttaggedUser.add(dbPost);
				}
			}

		});
		
		
		List<Post> groupReceivedBy = new ArrayList<Post>();
		// String statement2="match(a:Post) - [b:POST_RECEIVED_BY] -> (c:Group) -
		// [d:HAS_ASSOCIATED_MEMBER] -> (e:User) where id(e)=$createdBy and
		// a.isDeleted=false and a.isPrivate=true match(a:Post) <- [h:POST_CREATED_BY]
		// -(i:User) optional match(a:Post) - [g:HAS_ASSOCIATED_URL] -> (f:PostUrl)
		// return a,f,id(i)";
		Iterable<Map<String, Object>> dbgroupPostList = session.openSession()
				.query(Neo4jQueries.STR_POST_FOR_GROUP, Collections.singletonMap("createdBy", createdBy))
				.queryResults();
		dbgroupPostList.forEach(r -> {
			// System.out.println(r.get("a"));
			Post dbPost = (Post) r.get("a");
			dbPost.setCreatedBy((Long) r.get("id(i)"));
			dbPost.setStrCreatedBy((String) r.get("i.first_name") + " " + (String) r.get("i.last_name"));
			dbPost.setProfileImageUrl((String)r.get("i.profileImageUrl"));
			List<PostUrl> listUrl = new ArrayList<PostUrl>();
			listUrl.add((PostUrl) r.get("f"));
			if (groupReceivedBy.size() == 0) {
				dbPost.setUrlList(listUrl);
				groupReceivedBy.add(dbPost);
			} else {
				boolean dataMatched = false;
				for (int i = 0; i < groupReceivedBy.size(); i++) {
					Post p = groupReceivedBy.get(i);
					if (p.getId().compareTo(dbPost.getId()) == 0) {
						List<PostUrl> urllist = p.getUrlList();
						if (urllist != null) {
							urllist.add((PostUrl) r.get("f"));
							p.setUrlList(urllist);
						}
						dataMatched = true;
					}
				}
				if (!dataMatched) {
					dbPost.setUrlList(listUrl);
					groupReceivedBy.add(dbPost);
				}
			}

		});

		List<Post> publicPostList = new ArrayList<Post>();
		// String statement3="MATCH (n:Post) where n.isPrivate=false and
		// n.isDeleted=false match(n:Post) <- [p:POST_CREATED_BY] - (q:User) optional
		// match(n:Post) - [m:HAS_ASSOCIATED_URL] -> (o:PostUrl) return n,o,id(q)";
		Iterable<Map<String, Object>> dbpublicPostList = session.openSession()
				.query(Neo4jQueries.STR_POST_PUBLIC, Collections.singletonMap("createdBy", createdBy)).queryResults();
		dbpublicPostList.forEach(r -> {
			// System.out.println(r.get("n"));
			Post dbPost = (Post) r.get("n");
			dbPost.setCreatedBy((Long) r.get("id(q)"));
			dbPost.setStrCreatedBy((String) r.get("q.first_name") + " " + (String) r.get("q.last_name"));
			dbPost.setProfileImageUrl((String)r.get("q.profileImageUrl"));
			List<PostUrl> listUrl = new ArrayList<PostUrl>();
			listUrl.add((PostUrl) r.get("o"));
			if (publicPostList.size() == 0) {
				dbPost.setUrlList(listUrl);
				publicPostList.add(dbPost);
			} else {
				boolean dataMatched = false;
				for (int i = 0; i < publicPostList.size(); ++i) {
					Post p = publicPostList.get(i);
					if (p.getId().compareTo(dbPost.getId()) == 0) {
						List<PostUrl> urllist = p.getUrlList();
						if (urllist != null) {
							urllist.add((PostUrl) r.get("o"));
							p.setUrlList(urllist);
						}
						dataMatched = true;
					}
				}
				if (!dataMatched) {
					dbPost.setUrlList(listUrl);
					publicPostList.add(dbPost);
				}
			}
		});
		postcreatedBy.addAll(publicPostList);
		postcreatedBy.addAll(groupReceivedBy);
		postcreatedBy.addAll(posttaggedUser);
		/*
		 * postcreatedBy.forEach(pst->{ System.out.println("Post Id>>"+pst.getId()); });
		 */
		List<Post> uniqueResult = postcreatedBy.stream().collect(
				collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(Post::getId))), ArrayList::new));
		uniqueResult.sort(Comparator.comparing(Post::getCreatedDate).reversed());
		
		uniqueResult.forEach(ur->{
			//ur.setCommentList(postRepo.getPostCommentList(ur.getId()));
			List<Comment> postCommentList= new ArrayList<Comment>();
			Iterable<Map<String, Object>> dbpcommentList = session.openSession()
					.query(Neo4jQueries.STR_POST_COMMENT, Collections.singletonMap("postId", ur.getId())).queryResults();
			dbpcommentList.forEach(pcmt -> {
				Comment pcomment= (Comment)pcmt.get("g");
				pcomment.setCreatedBy((Long)pcmt.get("id(u)"));
				pcomment.setStrCreatedByName(pcmt.get("u.first_name")+" "+pcmt.get("u.last_name"));
				pcomment.setProfileImageUrl((String)pcmt.get("u.profileImageUrl"));
				postCommentList.add(pcomment);
			});
			ur.setCommentList(postCommentList);
			
			List<Long> tagUser= new ArrayList<Long>();
			Iterable<Map<String, Object>> tagUsersList = session.openSession()
					.query(Neo4jQueries.STR_POST_TAG_USERS, Collections.singletonMap("postId", ur.getId())).queryResults();
			
			tagUsersList.forEach(tulist -> {
				tagUser.add((Long)tulist.get("id(d)"));
			});
			ur.setTagUserlist(tagUser);
		});
		
		//tagUserList for Post
		
		List<PostDto> resultist= new ArrayList<PostDto>();
		
		uniqueResult.forEach(uresult->{
			PostDto pdto= new PostDto();
			BeanUtils.copyProperties(uresult, pdto);
			resultist.add(pdto);
		});
		
		
		return resultist;
	}

	@Override
	public Post viewPost(Long postId) {
		Optional<Post> post = postRepo.findById(postId);
		List<User> dbUser = postRepo.getCreatedByUser(postId);
		Post dbpost = new Post();
		if(post.isPresent()) {
			 post.get().setCreatedBy(dbUser.get(0).getId());
			 post.get().setStrCreatedBy(dbUser.get(0).getFirstName()+" "+dbUser.get(0).getLastName());
			 
			 dbpost=post.get();
			 dbpost.setProfileImageUrl(dbUser.get(0).getProfileImageUrl());
			 List<Comment> postCommentList= new ArrayList<Comment>();
				Iterable<Map<String, Object>> dbpcommentList = session.openSession()
						.query(Neo4jQueries.STR_POST_COMMENT, Collections.singletonMap("postId", dbpost.getId())).queryResults();
				dbpcommentList.forEach(pcmt -> {
					Comment pcomment= (Comment)pcmt.get("g");
					pcomment.setCreatedBy((Long)pcmt.get("id(u)"));
					pcomment.setStrCreatedByName(pcmt.get("u.first_name")+" "+pcmt.get("u.last_name"));
					pcomment.setProfileImageUrl((String)pcmt.get("u.profileImageUrl"));
					postCommentList.add(pcomment);
				});
				dbpost.setCommentList(postCommentList);
				
				List<Long> tagUser= new ArrayList<Long>();
				Iterable<Map<String, Object>> tagUsersList = session.openSession()
						.query(Neo4jQueries.STR_POST_TAG_USERS, Collections.singletonMap("postId", dbpost.getId())).queryResults();
				
				tagUsersList.forEach(tulist -> {
					tagUser.add((Long)tulist.get("id(d)"));
				});
				dbpost.setTagUserlist(tagUser);
			}
		return dbpost;
	}

	@Override
	public Resource loadpostFileAsResource(String fileName, Long postId) {
		// TODO Auto-generated method stub

		try {
			Path filePath = Paths.get(this.fileStorageLocation + "/post/" + String.valueOf(postId) + "/")
					.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new com.cts.cj.exception.MyFileNotFoundException("File not found " + fileName);
			}
		} catch (MalformedURLException ex) {
			throw new com.cts.cj.exception.MyFileNotFoundException("File not found " + fileName, ex);
		}

		// return null;
	}

	@Override
	public String postComment(CommentDto commentDto) {
		//MultipartFile[] uploadedFiles = commentDto.getFiles();
		Comment comment = new Comment();
	//	List<CommentUrl> urlList = new ArrayList<CommentUrl>();
		/*if (uploadedFiles != null) {
			for (MultipartFile file : uploadedFiles) {
				CommentUrl commentUrl = new CommentUrl();
				String fileName = file.getOriginalFilename();
				String extension = i2iCommonUtil.getFileExtension(fileName);
				Path filelocation = Paths
						.get(this.fileStorageLocation + "/postComment/" + String.valueOf(commentDto.getPostId()) + "/");
				try {
					Files.createDirectories(filelocation);
					Files.copy(file.getInputStream(), filelocation.resolve(fileName),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					return "{\"error\": \" Unable to upload file::" + e.getMessage() + " \"}";
				}
				if (CJCommonConstants.imageExtension.toUpperCase().contains(extension.toUpperCase())) {
					String imageUrl = CJCommonConstants.GROUP_ATTACHMENT_IMAGE_URL + String.valueOf(commentDto.getPostId())
							+ "/" + fileName;
					commentUrl.setUrl(imageUrl);
				}
				if (commentUrl.getUrl() == null) {
					return "{\"error\": \" Invalid file type to upload ::" + extension + " \"}";
				}
				urlList.add(commentUrl);
			}
			comment.setUrlList(urlList);
		}*/
		
	/*	Long postId = commentDto.getPostId();
		Long[] commentUrlIds = new Long[20];
		List<CommentUrl> urlListData = comment.getUrlList();
		int count = 0;
		for (CommentUrl url : urlListData) {
			commentUrlIds[count] = url.getId();
			count++;
		}*/
		
		Optional<Post> dbPost = postRepo.findById(commentDto.getPostId());
		if(dbPost.isPresent()) {
			comment.setComment(commentDto.getContent());
			comment.setCreatedDate(LocalDateTime.now());
			commentRepo.save(comment);
			commentRepo.relationOfPostAndComment(commentDto.getCommentedBy(), comment.getId(), commentDto.getPostId());
			//commentRepo.createHasAssociatedUrlRelationship(commentDto.getId(), commentUrlIds);
		}
		else {
			return "{\"error\": \" Post does not exist.\"}";
		}
		return "{\"success\": \" true\"}";
	}
	

	@Override
	public void tagUsers(Long postId, Long[] users) {
		// TODO Auto-generated method stub
		Iterable<Map<String, Object>> dbPostList = session.openSession()
				.query(Neo4jQueries.STR_POST_TAG_USERS,Collections.singletonMap("postId", postId ))
				.queryResults();
		dbPostList.forEach(r -> {
			Post dbPost = (Post) r.get("a");
			
		});
		//return data.get(0);
	}
	
	@Override
	public String deleteComment(Long commentId) {
		Optional<Comment> dbComment = commentRepo.findById(commentId);
		if (dbComment.isPresent()) {
			dbComment.get().setIsDeleted(Boolean.TRUE);
			commentRepo.save(dbComment.get());
		} else {
			return "{\"error\": \" Invalid comment to delete.\"}";
		}
		return "{\"success\": \" true\"}";
	}
}
