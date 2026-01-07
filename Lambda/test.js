import { handler } from "./index.js";

const event = {
  // queryStringParameters: {
  //   url: "https://dantri.com.vn/cong-nghe.htm",
  //   action: "get-articles"
  // }
  // queryStringParameters: {
  //   url: "https://dantri.com.vn/the-gioi/tong-thong-trump-canh-bao-lanh-dao-lam-thoi-venezuela-20260105054548305.htm",
  //   action: "get-article-content"
  // }
  queryStringParameters: {
    url: "https://dantri.com.vn/the-gioi/tong-thong-trump-canh-bao-lanh-dao-lam-thoi-venezuela-20260105054548305.htm",
    action: "summarize-article-content"
  }
};

handler(event).then(console.log);
