//package components
//
//import com.raquo.laminar.api.L._
//import markdown.Markdown
//
//import scala.language.{implicitConversions, postfixOps}
//import scala.util.Try
//
//object MarkdownUse {
//  implicit def markdown2HtmlElement(markdown: Markdown): HtmlElement =
//    renderMarkdown(markdown)
//
//  def renderMarkdown(markdown: Markdown): HtmlElement =
//    markdown match {
//      case Markdown.Document(mds) =>
//        div(
//          mds.map(renderMarkdown)
//        )
//      case Markdown.Paragraph(md) =>
//        p(
//          md.map(renderMarkdown)
//        )
//      case Markdown.Header(md, level) =>
//        renderMarkdown(md)
////        level match {
////          case 1 => h1(renderMarkdown(md), PageIndex.track(md.text, level))
////          case 2 => h2(renderMarkdown(md), PageIndex.track(md.text, level))
////          case 3 => h3(renderMarkdown(md), PageIndex.track(md.text, level))
////          case 4 => h4(renderMarkdown(md), PageIndex.track(md.text, level))
////          case _ => h5(renderMarkdown(md), PageIndex.track(md.text, level))
////        }
//      case Markdown.Strong(md) =>
//        strong(renderMarkdown(md))
//      case Markdown.Em(md) =>
//        em(renderMarkdown(md))
//      case Markdown.Text(string) =>
//        span(string)
//      case Markdown.InlineCode(code) =>
//        InlineCode(code)
//      case Markdown.CodeBlock(code, language) =>
//        CodeBlock(code)
//      case Markdown.Link(md, url) =>
//        // TODO: Improve this
//        Link(md.text)
////        Try(Routes.router.pageForRelativeUrl(url)).getOrElse(None) match {
////          case Some(page) =>
////            a(
////              cursor.pointer,
////              cls("small-caps"),
////              renderMarkdown(md),
////              onClick --> { _ => Routes.router.pushState(page) }
////            )
////          case None =>
////            a(
////              cls("small-caps"),
////              renderMarkdown(md),
////              href(url)
////            )
////        }
//    }
//}
