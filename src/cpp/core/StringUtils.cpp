/*
 * StringUtils.cpp
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#include <core/StringUtils.hpp>

#include <map>

#include <algorithm>
#include <boost/algorithm/string/replace.hpp>
#include <boost/regex.hpp>

namespace core {
namespace string_utils {   

void convertLineEndings(std::string* pStr, LineEnding type)
{
   std::string replacement;
   switch (type)
   {
   case LineEndingWindows:
      replacement = "\r\n";
      break;
   case LineEndingPosix:
      replacement = "\n";
      break;
   case LineEndingNative:
#if _WIN32
      replacement = "\r\n";
#else
      replacement = "\n";
#endif
      break;
   case LineEndingPassthrough:
   default:
      return;
   }

   *pStr = boost::regex_replace(*pStr, boost::regex("\\r?\\n|\\xE2\\x80[\\xA8\\xA9]"), replacement);
}

std::string toLower(const std::string& str)
{
   std::string lower = str;
   std::transform(lower.begin(), lower.end(), lower.begin(), ::tolower);
   return lower;
}
   
std::string textToHtml(const std::string& str)
{
   std::string html = str;
   boost::replace_all(html, "&", "&amp;");
   boost::replace_all(html, "<", "&lt;");
   return html;
}

namespace {
std::string escape(std::string specialChars,
                   const std::map<char, std::string>& replacements,
                   std::string str)
{
   std::string result;
   result.reserve(static_cast<size_t>(str.size() * 1.2));

   size_t tail = 0;
   for (size_t head = 0;
        head < str.size()
           && str.npos != (head = str.find_first_of(specialChars, head));
        tail = ++head)
   {
      if (tail < head)
         result.append(str, tail, head - tail);

      result.append(replacements.find(str.at(head))->second);
   }

   if (tail < str.size())
      result.append(str, tail, std::string::npos);

   return result;

}
} // anonymous namespace

std::string htmlEscape(const std::string& str, bool isAttributeValue)
{
   std::string escapes = isAttributeValue ?
                         "<>&'\"\r\n" :
                         "<>&" ;

   std::map<char, std::string> subs;
   subs['<'] = "&lt;";
   subs['>'] = "&gt;";
   subs['&'] = "&amp;";
   if (isAttributeValue)
   {
      subs['\''] = "&#39;";
      subs['"'] = "&quot;";
      subs['\r'] = "&#13;";
      subs['\n'] = "&#10;";
   }

   return escape(escapes, subs, str);
}

std::string jsLiteralEscape(const std::string& str)
{
   std::string escapes = "\\'\"\r\n<";

   std::map<char, std::string> subs;
   subs['\\'] = "\\\\";
   subs['\''] = "\\'";
   subs['"'] = "\\\"";
   subs['\r'] = "\\r";
   subs['\n'] = "\\n";
   subs['<'] = "\074";

   return escape(escapes, subs, str);
}

} // namespace string_utils
} // namespace core 



