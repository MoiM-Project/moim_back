package data.member;

import data.config.BaseException;
import data.config.BaseResponse;
import data.config.BaseResponseStatus;
import data.config.JwtTokenUtil;
import data.dto.BookingDetailDto;
import data.dto.MemberDto;
import data.mapper.MemberMapper;
import data.mapper.SellerMapper;
import data.member.model.*;
import data.seller.PostSellerReq;
import data.util.ChangeName;
import data.util.FileUtil;
import data.util.S3UploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static data.config.BaseResponseStatus.*;
import static data.util.Validation.isValidatedIdx;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/member")
//@RequiredArgsConstructor
public class MemberController {
    private final MemberDao memberDao;
//    private final S3UploadUtil s3UploadUtil;

    @Autowired
    S3UploadUtil s3UploadUtil;

    String uploadFileName;
    ArrayList<String> uploadFileNames = new ArrayList<>();

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MemberService memberService;

    @Autowired
    SellerMapper sellerMapper;

    @Autowired
    MemberMapper memberMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private EmailCertService emailCertService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    public MemberController(MemberDao memberDao) {
        this.memberDao = memberDao;
    }


    @ResponseBody
    @PostMapping("/signup")
    public BaseResponse<PostMemberRes> createMember(@RequestBody PostMemberReq postMemberReq) {

        try {
            if (postMemberReq.getNickname() == null) {
                return new BaseResponse<>(POST_USER_NICKNAME_NULL);
            }

            String token = UUID.randomUUID().toString();
            System.out.println("====== Controller : createMember : Req ====== " + postMemberReq);

            PostMemberRes postMemberRes = memberService.createMember(postMemberReq, token);
            UserDetails userDetails = memberService.findByEmailStatusZero(postMemberReq.getEmail());

            final String jwt = jwtTokenUtil.generateToken(userDetails);

            emailCertService.createEmailConfirmationToken(token, postMemberReq.getEmail(), jwt);

            return new BaseResponse<>(postMemberRes);

        } catch (Exception exception) {
            System.out.println(exception);
            return new BaseResponse<>(BaseResponseStatus.FAIL);
        }
    }

    @ResponseBody
    @GetMapping("/confirm")
    public RedirectView signupConfirm(GetEmailConfirmReq getEmailConfirmReq) throws Exception {
        GetEmailCertRes getEmailCertRes = emailCertService.signupConfirm(getEmailConfirmReq);
        return new RedirectView("http://localhost:3000/emailconfirm/" + getEmailConfirmReq.getJwt());
    }

    @ResponseBody
    @GetMapping("/modify")
    public BaseResponse<GetMemberRes> getModifyMemberInfo(@AuthenticationPrincipal UserLoginRes userLoginRes) {

        if (userLoginRes == null) {
            return new BaseResponse<>(NOT_LOGIN);
        }
        try {
            BigInteger userIdx = userLoginRes.getIdx();
            GetMemberRes getMemberRes = memberService.getModifyMemberInfo(userIdx);
            return new BaseResponse<>(getMemberRes);

        } catch (Exception exception) {
            return new BaseResponse<>(EMPTY_IDX);
        }
    }


    @ResponseBody
    @PatchMapping("/modify/{idx}")
    public BaseResponse<String> modifyMemberInfo(@AuthenticationPrincipal UserLoginRes userLoginRes, @PathVariable("idx") BigInteger idx, @RequestBody PatchMemberModityReq patchMemberModityReq) {

        if (idx == null) {
            return new BaseResponse<>(EMPTY_IDX);
        }
        if (!isValidatedIdx(idx)) {
            return new BaseResponse<>(INVALID_IDX);
        }
        if (userLoginRes == null) {
            return new BaseResponse<>(NOT_LOGIN);
        }

        try {
            BigInteger userIdx = userLoginRes.getIdx();
            System.out.println("== userLoginRes.getIdx: " + userLoginRes.getIdx() + ", Idx: " + idx);

            if (!userIdx.equals(idx)) {
                return new BaseResponse<>(INVALID_USER_JWT);
            }

            memberService.modifyMemberInfo(patchMemberModityReq, idx);
            String result = patchMemberModityReq.getNickname() + "??? ?????? ??????.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @ResponseBody
    @PostMapping("/sellersignup")
    public BaseResponse<PostMemberRes> createSeller(@RequestBody PostSellerReq postSellerReq) {

        try {
            System.out.println("========================== Req: " + postSellerReq);

            String token = UUID.randomUUID().toString();
            PostMemberRes postMemberRes = memberService.createSeller(postSellerReq,token);
            return new BaseResponse<>(postMemberRes);

        } catch (Exception exception) {
            System.out.println(exception);
            return new BaseResponse<>(BaseResponseStatus.FAIL);
        }
    }

    @PostMapping("/Sellercheck")
    public Map<String,Object> getLogin(@RequestBody Map<String,String> map) {
        System.out.println("check id:"+map.get("email"));
        System.out.println("check pw:"+passwordEncoder.encode(map.get("password")));
//        int check = sellerMapper.getLogin(map);  // ???????????? ????????? ????????? 1 ??????, ????????? 0 ??????
        int check = 1;  // ???????????? ????????? ????????? 1 ??????, ????????? 0 ??????
        System.out.println(check);
        // ????????? ??????????????? ????????????
        String name="";
        String num="";
        if(check==1){  // ????????????
            name = sellerMapper.getName(map.get("email"));
            num = sellerMapper.getNum(map.get("email"));
        }
        Map<String, Object> sendmap = new HashMap<>();
        sendmap.put("check", check);
        sendmap.put("name", name);
        sendmap.put("num", num);
        return sendmap;
    }

    @GetMapping("/getMemberInfo")
    public MemberDto getMemberInfo(
            @RequestParam int idx)
    {
        //????????? Notice ?????? ??????
        System.out.println("num??? ?????? = " + idx);

        //num ??? ??????
        return memberMapper.getMemberInfo(idx);
    }

//    @PostMapping("/modify/profileImage")
//    public void insertTheme (@RequestParam("updateFile") MultipartFile multipartFile,
//                             int idx
//    ) throws IOException {
//
//        HashMap<String, Object> map = new HashMap<>();
//
//        map.put("idx",idx);
//        map.put("file",s3UploadUtil.upload(multipartFile,"user"));
//
//        System.out.println(map);
//        memberMapper.profileUpdate(map);
//    }

    @PatchMapping("/modify/profileImage")
    public void updateTheme (@RequestParam(value="updateFile", required = false) MultipartFile multipartFile,
                             int idx
    ) throws IOException {

        HashMap<String, Object> map = new HashMap<>();

        map.put("idx",idx);

        // ??????????????? ?????? ?????? ??????
        if(multipartFile!=null){
            // ??????????????? ?????????
            if(memberMapper.getMemberInfo(idx).getProfile_image()!=null){
                // db??? ????????? url??? ?????? ??????
                // ?????? ???????????? ?????????url??? ????????? ??? s3?????? ??????
                String path = memberMapper.getMemberInfo(idx).getProfile_image().split("/",4)[3];
                s3UploadUtil.delete(path);
            }
            // ????????? ????????? s3??? ????????? ??? map??? ??????
            map.put("updateFile",s3UploadUtil.upload(multipartFile,"user"));
        } else {
            // ??????????????? ????????????
            // db??? ????????? url ???????????? map??? ??????
            map.put("updateFile",memberMapper.getMemberInfo(idx).getProfile_image());
        }
        System.out.println("????????????");
        System.out.println(map);

        memberMapper.profileUpdate(map);
        System.out.println(map);
    }

    // ????????? ?????? ??????
//    @PostMapping("/modify/profileImage")
//    public void noticeInsert (@RequestBody MultipartFile updateFile,
//                              HttpServletRequest request,
//                              @RequestParam String oldPhoto,
//                              int idx
//    ){
//
//        //DB??? update???????????? map ??????
//        HashMap<String, Object> map = new HashMap<>();
//
//        //uploadFile??? ???????????? map??? ??????
//        map.put("idx",idx);
//
//        //????????? ??????????????? ???????????? ??????
//        try {
//
//            //upload ??????????????? ?????????
//            if(updateFile != null) {
//
//                // ???????????? ????????? ??????(path) ?????????
//                String path = request.getSession().getServletContext().getRealPath("/image");
//
//                //?????? ????????? ????????? ?????? ?????? path ???????????? ?????? ?????? ??? ?????? ?????????
//                if (oldPhoto != null) {
//                    FileUtil.deletePhoto(path, oldPhoto);   //?????? ?????? path ????????? oldPhoto ??? ?????????
//                    System.out.println("?????? ?????? oldPhoto ?????? ??????");
//                }
//
//                //????????? ????????? ????????? ??????
//                uploadFileName = updateFile.getOriginalFilename();
//
//                //???????????? ?????????????????? ??????(util ??????)
//                uploadFileName = ChangeName.getChangeFileName(updateFile.getOriginalFilename());
//
//                //path ????????? uploadFileName ??? ??????????????? ????????? ??????
//                updateFile.transferTo(new File(path + "/" + uploadFileName));
//
//                //?????? ??? ????????? ??????
//                System.out.println("?????? ????????? ????????? ?????? -> ?????? // ????????? " + path + "//" +uploadFileName );
//
//                //map ?????? updateFile??? ????????? ??????
//                map.put("updateFile",uploadFileName);
//
//                System.out.println(map);
//                System.out.println("?????? ??????");
//                //upload ??????????????? ????????????
//            }else {
//
//                //map ?????? updateFile??? ?????? ??????(oldPhoto) ?????? ??????
//                map.put("updateFile",oldPhoto);
//                System.out.println("?????? ?????? ??????!");
//            }
//
//        } catch (IllegalStateException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//
//        // insert sql ??? map ??????
//        memberMapper.profileUpdate(map);
//    }

    //  ???????????? ?????? API
    @PostMapping("/updatePassword")
    public void updatePassword(@RequestParam String password,@RequestParam String email) {
//        System.out.println("update email ?????? = "+email);
        System.out.println("?????? ??????");
        memberService.updatePassword(password, email);
    }

    @PostMapping("/updateNickname")
    public void updateNickname(@RequestParam int idx,@RequestParam String nickname){

        HashMap<String, Object> map = new HashMap<>();
        map.put("idx",idx);
        map.put("nickname",nickname);
        memberMapper.updateNickname(map);
    }

    // ?????? ?????? ??????
    @GetMapping("/checksocial")
    public String checkSocial(@RequestParam String email){
        System.out.println("????????? ?????????="+email);

        String check = "normal";
        if(memberMapper.searchSocial(email).equals("kakao")){
            check = "social";
        }
        return check;
    }

    //  ?????? ?????? API
    @DeleteMapping("/delete")
    public void deleteUser(@RequestParam int idx) {
        System.out.println("delete num??? ?????? = "+idx);
        System.out.println("?????? ??????");
        memberMapper.deleteMember(idx);
    }

    @ResponseBody
    @GetMapping("{email}")
    public Boolean getUserEmail(@PathVariable("email") String email) {
        Boolean result = memberService.getUserEmail(email);
        return result;
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        if (authenticationRequest.getUsername().length() == 0) {
            System.out.println("username is NULL");
        }

        if (authenticationRequest.getPassword().length() == 0) {
            System.out.println("Password is NULL");
        }

// ????????? ???????????? ??????.
//        if (!memberDao.isValidStatus(authenticationRequest)) {
//            System.out.println("????????? ??????");
//        }

        System.out.println(authenticationRequest.getUsername() + ", " + authenticationRequest.getPassword());

        Authentication authentication = authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        UserLoginRes userLoginRes = (UserLoginRes) authentication.getPrincipal();

        final String token = jwtTokenUtil.generateToken(userLoginRes);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    private Authentication authenticate(String username, String password) throws Exception {

        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new DisabledException("USER_DISABLED", e);
        } catch (AccountExpiredException e) {
            throw new AccountExpiredException("AccountExpiredException", e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("???????????? ?????? ?????????. INVALID_CREDENTIALS", e);
        } catch (InternalAuthenticationServiceException e) {
            throw new InternalAuthenticationServiceException("???????????? ?????? ????????? ?????????. InternalAuthenticationServiceException", e);
        }
        catch (AuthenticationCredentialsNotFoundException e) {
            throw new AuthenticationCredentialsNotFoundException("?????? ?????? ??????.", e);
        }
    }


}
