package org.jetbrains.plugins.scala.lang.parser.parsing.base {

  import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
  import org.jetbrains.plugins.scala.lang.lexer.ScalaElementType
  import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
  import org.jetbrains.plugins.scala.lang.parser.parsing.types.StableId
  import org.jetbrains.plugins.scala.lang.parser.parsing.types.StableIdInImport
  import org.jetbrains.plugins.scala.lang.parser.parsing.expressions.Exprs
  import org.jetbrains.plugins.scala.lang.parser.util.ParserUtils
  import org.jetbrains.plugins.scala.util.DebugPrint
  import org.jetbrains.plugins.scala.lang.parser.parsing.types.StableId
  import org.jetbrains.plugins.scala.lang.parser.bnf.BNF
  import org.jetbrains.plugins.scala.lang.parser.parsing.types.Type

  import com.intellij.psi.tree.TokenSet
  import com.intellij.lang.PsiBuilder
  import com.intellij.psi.tree.IElementType

  /**
  * User: Dmitry.Krasilschikov
  * Date: 17.10.2006
  * Time: 11:38:56
  */


  object StatementSeparator extends ConstrWithoutNode {
    override def parseBody(builder: PsiBuilder): Unit = {
      builder.getTokenType match {
        case ScalaTokenTypes.tSEMICOLON => {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tSEMICOLON)
        }

        case ScalaTokenTypes.tLINE_TERMINATOR => {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tLINE_TERMINATOR)
        }

        case _ => {
          builder.error("wrong statement separator")
          return
        }
      }
    }
  }

  object AttributeClauses extends Constr {
    override def getElementType = ScalaElementTypes.ATTRIBUTE_CLAUSES

    override def parseBody(builder: PsiBuilder): Unit = {
      while (BNF.firstAttributeClause.contains(builder.getTokenType)){
        AttributeClause parse builder
      }
    }
  }

  /*
      AttributeClause ::=  [  Attribute { ,  Attribute}  ]  [NewLine]
  */

  object AttributeClause extends ConstrItem {
    override def first: TokenSet = TokenSet.create(Array(ScalaTokenTypes.tLSQBRACKET))

    override def getElementType = ScalaElementTypes.ATTRIBUTE_CLAUSE;

    override def parseBody(builder: PsiBuilder): Unit = {

      builder.getTokenType() match {
        case ScalaTokenTypes.tLSQBRACKET => {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tLSQBRACKET)

          Attribute.parse(builder)

          //possible attributes, separated by comma
          while (ScalaTokenTypes.tCOMMA.equals(builder.getTokenType)) {
            ParserUtils.eatElement(builder, ScalaTokenTypes.tCOMMA)

            Attribute.parse(builder)
          }

          //expected right square brace
          if (ScalaTokenTypes.tRSQBRACKET.equals(builder.getTokenType)) {
            ParserUtils.eatElement(builder, ScalaTokenTypes.tRSQBRACKET)
          } else {
            builder.error("']' expected")
            return
          }

          builder.getTokenType() match {
            //possible line terminator
            case ScalaTokenTypes.tLINE_TERMINATOR => {
              ParserUtils.eatElement(builder, ScalaTokenTypes.tLINE_TERMINATOR)
            }

            case _ => {}
          }
        }

        case _ => {
          builder.error("wrong statement separator")
          return
        }
      }
    }

  }

  /*
  *  Attribute ::= Constr
  */

  object Attribute extends Constr{
    override def getElementType = ScalaElementTypes.ATTRIBUTE

    override def parseBody(builder: PsiBuilder): Unit = {
      Constructor.parse(builder)
    }
  }

  /*
  *   Constr ::= StableId [TypeArgs] { (  [Exprs]  ) }
  */
  object Constructor extends Constr{
    override def getElementType = ScalaElementTypes.CONSTRUCTOR

    override def parseBody(builder: PsiBuilder): Unit = {

      if (BNF.firstStableId.contains(builder.getTokenType)) {
        StableId.parse(builder)
      } else {
        builder.error("identifier expected")
        return
      }

      if (ScalaTokenTypes.tLSQBRACKET.equals(builder.getTokenType)) {
        TypeArgs.parse(builder)
      }

      while (ScalaTokenTypes.tLPARENTHESIS.equals(builder.getTokenType)) {
        if (ScalaTokenTypes.tLPARENTHESIS.equals(builder.getTokenType)) {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tLPARENTHESIS)
        } else {
          builder.error("'(' expected")
          return
        }

        if (BNF.firstExpr.contains(builder.getTokenType)) {
          val args = builder.mark()
          Exprs.parse(builder, null)
          args.done(ScalaElementTypes.ARG_EXPRS)
        }

        if (ScalaTokenTypes.tRPARENTHESIS.equals(builder.getTokenType)) {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tRPARENTHESIS)
        } else if (ScalaTokenTypes.tCOLON.equals(builder.getTokenType)){
          // Suppose, that this is construction like
          // (... expr : _* )
          ParserUtils.eatElement(builder, builder.getTokenType)
          if (ScalaTokenTypes.tUNDER.equals(builder.getTokenType) && // _ ...
          {
            ParserUtils.eatElement(builder, builder.getTokenType)
            "*".equals(builder.getTokenText)                   // _* ...
          } &&
          {
            ParserUtils.eatElement(builder, builder.getTokenType)
            ScalaTokenTypes.tRPARENTHESIS.equals(builder.getTokenType)
          }) {
            ParserUtils.eatElement(builder, ScalaTokenTypes.tRPARENTHESIS)
          } else {
            builder.error("Sequence argument or ) expected")
          }
        } else {
          builder.error("')' expected ")
          return
        }
      }
    }
  }

  /*
     TypeArgs :== '[' Types']'
  */

  object TypeArgs extends Constr{
    override def getElementType = ScalaElementTypes.TYPE_ARGS

    override def parseBody(builder: PsiBuilder): Unit = {
      if (ScalaTokenTypes.tLSQBRACKET.equals(builder.getTokenType)) {
        ParserUtils.eatElement(builder, ScalaTokenTypes.tLSQBRACKET)

        Types.parse(builder)

        if (ScalaTokenTypes.tRSQBRACKET.equals(builder.getTokenType)) {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tRSQBRACKET)
        } else {
          builder.error("']' expected")
          return
        }
      }
    }
  }

  /*
  *   types :== Type {',' Type}
  */

  object Types extends Constr{
    override def getElementType = ScalaElementTypes.TYPES
    override def parseBody(builder: PsiBuilder): Unit = {
      if (BNF.firstType.contains(builder.getTokenType)){
        Type parse builder

        while (ScalaTokenTypes.tCOMMA.equals(builder.getTokenType)) {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tCOMMA)

          if (BNF.firstType.contains(builder.getTokenType)) {
            Type parse builder
          } else {
            builder error "identifier expected"
            return
          }
        }
      }
    }
  }

  /*
  *   Modifier ::= LocalModifier
  *             | override
  *             | private [ "[" id "]" ]
  *             | protected [ "[" id "]" ]
  */

  object Modifier extends ConstrWithoutNode {
    override def parseBody(builder: PsiBuilder): Unit = {
      if (BNF.firstLocalModifier.contains(builder.getTokenType)) {
        LocalModifier.parse(builder)
      }

      if (ScalaTokenTypes.kOVERRIDE.equals(builder.getTokenType)) {
        ParserUtils.eatElement(builder, ScalaTokenTypes.kOVERRIDE)
      }

      if (BNF.firstAccessModifier.contains(builder.getTokenType)) {
        AccessModifier.parse(builder)
      }

    }
  }

  object ModifierWithoutImplicit extends ConstrWithoutNode {
    override def parseBody(builder: PsiBuilder): Unit = {
      if (BNF.firstLocalModifier.contains(builder.getTokenType)) {
        LocalModifierWithoutImplicit.parse(builder)
      }

      if (ScalaTokenTypes.kOVERRIDE.equals(builder.getTokenType)) {
        ParserUtils.eatElement(builder, ScalaTokenTypes.kOVERRIDE)
      }

      if (BNF.firstAccessModifier.contains(builder.getTokenType)) {
        AccessModifier.parse(builder)
      }

    }
  }

  object Modifiers extends ConstrUnpredict {
    override def parseBody(builder: PsiBuilder): Unit = {
      val modifiersMarker = builder.mark
      var numberOfModifiers = 0;

      while (BNF.firstModifier.contains(builder.getTokenType)) {
        Modifier parse builder
        numberOfModifiers = numberOfModifiers + 1
      }

      if (numberOfModifiers > 1) modifiersMarker.done(ScalaElementTypes.MODIFIERS)
      else (modifiersMarker.drop)
    }
  }

  /*
  *  AccessModifier ::= private [ "[" id "]" ]
  *                   | protected [ "[" id "]" ]
  */

  object AccessModifier extends ConstrWithoutNode {
    override def parseBody(builder: PsiBuilder): Unit = {
      builder.getTokenType match {
        case ScalaTokenTypes.kPRIVATE => ParserUtils.eatElement(builder, ScalaTokenTypes.kPRIVATE)
        case ScalaTokenTypes.kPROTECTED => ParserUtils.eatElement(builder, ScalaTokenTypes.kPROTECTED)
      }

      if (ScalaTokenTypes.tLSQBRACKET.equals(builder.getTokenType)){
        ParserUtils.eatElement(builder, ScalaTokenTypes.tLSQBRACKET)

        if (ScalaTokenTypes.tIDENTIFIER.equals(builder.getTokenType)) {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tIDENTIFIER)
        } else {
          builder.error("identifier expected")
          return
        }

        if (ScalaTokenTypes.tRSQBRACKET.equals(builder.getTokenType)){
          ParserUtils.eatElement(builder, ScalaTokenTypes.tRSQBRACKET)
        } else {
          builder.error("']' expected")
          return
        }
      }
    }
  }

  /*
  *  LocalModifier ::= abstract
  *                  | final
  *                  | sealed
  *                  | implicit
  */

  object LocalModifier extends ConstrWithoutNode {
    override def parseBody(builder: PsiBuilder): Unit = {
      if (BNF.firstLocalModifier.contains(builder.getTokenType)) {
        builder.getTokenType() match {
          case ScalaTokenTypes.kABSTRACT => ParserUtils.eatElement(builder, ScalaTokenTypes.kABSTRACT)

          case ScalaTokenTypes.kFINAL => ParserUtils.eatElement(builder, ScalaTokenTypes.kFINAL)

          case ScalaTokenTypes.kSEALED => ParserUtils.eatElement(builder, ScalaTokenTypes.kSEALED)

          case ScalaTokenTypes.kIMPLICIT => ParserUtils.eatElement(builder, ScalaTokenTypes.kIMPLICIT)

          case _ => builder error "local modifier expected"
        }

      } else {
        builder error "local modifier expected"
        return
      }

    }
  }

  object LocalModifierWithoutImplicit extends ConstrWithoutNode {
    override def parseBody(builder: PsiBuilder): Unit = {
      if (BNF.firstLocalModifier.contains(builder.getTokenType)) {
        builder.getTokenType() match {
          case ScalaTokenTypes.kABSTRACT => ParserUtils.eatElement(builder, ScalaTokenTypes.kABSTRACT)

          case ScalaTokenTypes.kFINAL => ParserUtils.eatElement(builder, ScalaTokenTypes.kFINAL)

          case ScalaTokenTypes.kSEALED => ParserUtils.eatElement(builder, ScalaTokenTypes.kSEALED)

          case _ => builder error "local modifier expected"
        }

      } else {
        builder error "local modifier expected"
        return
      }

    }
  }

  /*
  *  Import ::= import ImportExpr { ,  ImportExpr}
  */

  object Import extends Constr {
    override def getElementType = ScalaElementTypes.IMPORT_STMT

    override def parseBody(builder: PsiBuilder): Unit = {
      if (ScalaTokenTypes.kIMPORT.equals(builder.getTokenType)) {
        ParserUtils.eatElement(builder, ScalaTokenTypes.kIMPORT)

        ParserUtils.listOfSmth(builder, ImportExpr, ScalaTokenTypes.tCOMMA, ScalaElementTypes.IMPORT_EXPRS)

      } else {
        builder.error("'import' expected")
        return
      }
    }

    /*
    *  ImportExpr ::= StableId  .  (id |  _  | ImportSelectors)
    */

    object ImportExpr extends ConstrItem {
      override def getElementType = ScalaElementTypes.IMPORT_EXPR

      override def first = TokenSet.create(Array(ScalaTokenTypes.tIDENTIFIER))

      override def parseBody(builder: PsiBuilder): Unit = {

        if (ScalaTokenTypes.tIDENTIFIER.equals(builder.getTokenType)) {
          StableIdInImport.parse(builder)
        } else {
          builder.error("identifier expected ")
          return
        }

        if (ScalaTokenTypes.tDOT.equals(builder.getTokenType)) {
          ParserUtils.eatElement(builder, ScalaTokenTypes.tDOT)
        } else {
          builder.error("'.' expected ")
          return
        }

        val endMarker = builder.mark()
        builder.getTokenType() match {
          case ScalaTokenTypes.tIDENTIFIER => {
            ParserUtils.eatElement(builder, ScalaTokenTypes.tIDENTIFIER)
            endMarker.done(ScalaElementTypes.IMPORT_END)
            return
          }

          case ScalaTokenTypes.tUNDER => {
            endMarker.drop()
            ParserUtils.eatElement(builder, ScalaTokenTypes.tUNDER)
            return
          }

          case ScalaTokenTypes.tLBRACE => {
            endMarker.drop()
            ImportSelectors.parse(builder)
            return
          }

          case _ => {
            endMarker.drop()
            builder.error("identifier, import selectors or '_' expected")
            return
          }
        }
      }
    }

    /*
    *  ImportSelectors ::=  {  {ImportSelector  , } (ImportSelector |  _ )  }
    */

    object ImportSelectors extends Constr{
      override def getElementType = ScalaElementTypes.IMPORT_SELECTORS

      override def parseBody(builder: PsiBuilder): Unit = {

        def parseImportSelectorsWithoutBraces: Unit = {
          if (BNF.firstImportSelector.contains(builder.getTokenType)) {
            ImportSelector parse builder

            //if there is a list of import selectors
            while (ScalaTokenTypes.tCOMMA.equals(builder.getTokenType)){
              ParserUtils.eatElement(builder, ScalaTokenTypes.tCOMMA)

              //found '_', return because '_' have to be the last token in import selectors construction
              if (ScalaTokenTypes.tUNDER.equals(builder.getTokenType)){
                val underMarker = builder.mark()
                builder.advanceLexer
                underMarker.done(ScalaElementTypes.IMPORT_SELECTOR)
                return
              }

              if (BNF.firstImportSelector.contains(builder.getTokenType)){
                ImportSelector parse builder
              }
            }
            return
          }

          if (ScalaTokenTypes.tUNDER.equals(builder.getTokenType)){
            val underMarker = builder.mark()
            builder.advanceLexer
            underMarker.done(ScalaElementTypes.IMPORT_SELECTOR)
            return
          }

          //import selector or '_' not found. Error
          builder error "import selector expected"
          return
        }

        if (ScalaTokenTypes.tLBRACE.equals(builder.getTokenType)){
          ParserUtils.eatElement(builder, ScalaTokenTypes.tLBRACE)
        } else {
          builder error "'{' expected"
          return
        }

        parseImportSelectorsWithoutBraces

        if (ScalaTokenTypes.tRBRACE.equals(builder.getTokenType)){
          ParserUtils.eatElement(builder, ScalaTokenTypes.tRBRACE)
        } else {
          builder error "'}' expected"
          return
        }

      }
    }

    /*
    *  ImportSelector ::= id [ =>  id |  =>   _ ]
    */


    object ImportSelector extends Constr{
      override def getElementType = ScalaElementTypes.IMPORT_SELECTOR

      override def parseBody(builder: PsiBuilder): Unit = {

        if (BNF.firstImportSelector.contains(builder.getTokenType)){

          val sel = builder.mark()
          ParserUtils.eatElement(builder, ScalaTokenTypes.tIDENTIFIER)
          sel.done(ScalaElementTypes.IMPORT_SELECTOR_BEGIN)


          if (ScalaTokenTypes.tFUNTYPE.equals(builder.getTokenType)) {
            ParserUtils.eatElement(builder, ScalaTokenTypes.tFUNTYPE)

            builder.getTokenType() match {
              case ScalaTokenTypes.tIDENTIFIER => {
                ParserUtils.eatElement(builder, ScalaTokenTypes.tIDENTIFIER)
              }

              case ScalaTokenTypes.tUNDER => {
                ParserUtils.eatElement(builder, ScalaTokenTypes.tUNDER)
              }

              case _ => {
                builder.error("identifier or '_' expected")
                return
              }
            }

          }

        } else {
          builder error "import selector expected"
          return
        }
      }
    }
  }

  /*
  *  ids ::= id { ,  id}
  */


  object Ids extends ConstrUnpredict {

    override def parseBody(builder: PsiBuilder): Unit = {
      val idListmarker = builder.mark()

      if (ScalaTokenTypes.tIDENTIFIER.equals(builder.getTokenType)) {
        val marker = builder.mark()
        ParserUtils.eatElement(builder, ScalaTokenTypes.tIDENTIFIER)
        marker.done(ScalaElementTypes.REFERENCE)
      } else {
        builder error "identifier expected"
        return
      }

      var hasIdList = false;
      while (ScalaTokenTypes.tCOMMA.equals(builder.getTokenType)) {
        ParserUtils.eatElement(builder, ScalaTokenTypes.tCOMMA)

        if (ScalaTokenTypes.tIDENTIFIER.equals(builder.getTokenType)) {
          val marker = builder.mark()
          ParserUtils.eatElement(builder, ScalaTokenTypes.tIDENTIFIER)
          marker.done(ScalaElementTypes.REFERENCE)
          hasIdList = true;
        } else {
          builder error "identifier expected"
          idListmarker.drop()
          return
        }
      }

      if (hasIdList) idListmarker.done(ScalaElementTypes.IDENTIFIER_LIST)
      else idListmarker.drop
    }
  }
}