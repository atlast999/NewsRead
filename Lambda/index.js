import * as cheerio from "cheerio";
import { GoogleGenAI } from "@google/genai";
/**
 * Lambda handler
 */
export const handler = async (event) => {
  try {
    const params = event.queryStringParameters || {};
    const url = params.url;
    const action = params.action;

    if (!url) {
      return {
        statusCode: 400,
        body: "Missing 'url' parameter"
      };
    }

    if (action === "get-articles") {
      const result = await getArticles(url);
      return {
        statusCode: 200,
        body: JSON.stringify(result)
      };
    }

    if (action === "get-article-content") {
      const result = await getArticleContent(url);
      return {
        statusCode: 200,
        body: JSON.stringify(result)
      };
    }

    if (action === "summarize-article-content") {
      const article = await getArticleContent(url);
      if (!article) {
        return {
          statusCode: 404,
          body: "Article Content not found"
        };
      }
      const summary = await summarizeArticle(article);
      return {
        statusCode: 200,
        body: JSON.stringify({
          summary: summary,
        })
      };
    }

    return {
      statusCode: 400,
      body: "Unsupported action"
    };
  } catch (err) {
    console.error(err);
    return {
      statusCode: 500,
      body: "Internal server error"
    };
  }
};

/**
 * Fetches and parses articles from a given URL
 */
async function getArticles(url) {
  const response = await fetch(url, {
    headers: {
      "User-Agent": "Mozilla/5.0 (compatible; LambdaScraper/1.0)",
      "Accept-Language": "vi-VN,vi;q=0.9,en;q=0.8"
    }
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch URL with status ${response.status}`);
  }

  const html = await response.text();
  const $ = cheerio.load(html);

  const articles = [];

  $("article.article-item").each((_, el) => {
    const titleEl = $(el).find("h3.article-title a").first();
    const summaryEl = $(el)
      .find(".article-excerpt a[data-prop='sapo']")
      .first();
    const imgEl = $(el).find(".article-thumb img").first();

    articles.push({
      title: titleEl.text().trim() || null,
      url: titleEl.attr("href") || null,
      summary: summaryEl.text().trim() || null,
      thumbnail:
        imgEl.attr("data-src") ||
        imgEl.attr("src") ||
        null
    });
  });

  return {
    count: articles.length,
    articles
  };
}

/**
 * Fetches and parses article content from a given URL
 */
async function getArticleContent(url) {
  console.log("getArticleContent....")
  const response = await fetch(url, {
    headers: {
      "User-Agent": "Mozilla/5.0 (compatible; LambdaScraper/1.0)",
      "Accept-Language": "vi-VN,vi;q=0.9,en;q=0.8"
    }
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch URL with status ${response.status}`);
  }

  const html = await response.text();
  const $ = cheerio.load(html);

  const articleEl = $("article.singular-container").first();

  if (!articleEl.length) {
    return null;
  }

  const contentText = articleEl
  .find(".singular-content")
  .text()
  .replace(/\s+/g, " ")
  .trim();

  return {
    title: articleEl.find("h1.title-page").first().text().trim() || null,
    overview: articleEl.find("h2.singular-sapo").first().text().trim() || null,
    content: contentText,
  };
}

async function summarizeArticle(article) {
  console.log("summarizeArticle....")
  const ai = new GoogleGenAI({
    apiKey: process.env.GEMINI_API_KEY
  });
  const prompt = `
Hãy tóm tắt nội dung bài viết dưới đây với các yêu cầu sau:
- 3 gạch đầu dòng
- Văn phong trung lập, dễ hiểu
- Không quá 100 từ
- Không sử dụng emoji
Nội dung bài viết:
  ${JSON.stringify(article)}
`;
  console.log(prompt)
  const response = await ai.models.generateContent({
    model: "gemini-2.5-flash",
    contents: prompt,
    config: {
      systemInstruction: "Bạn là một chuyên gia tóm tắt bài viết.",
    },
  });
  console.log(response.text);
  return response.text
}