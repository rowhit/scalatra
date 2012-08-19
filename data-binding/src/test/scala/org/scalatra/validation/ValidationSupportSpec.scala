package org.scalatra
package validation

import org.specs2.mutable.Specification
import net.liftweb.json.{DefaultFormats, Formats}
import databinding.{FieldBinding, WithBindingFromParams}
import scalaz._
import Scalaz._
import org.scalatra.databinding.BindingSyntax._

class WithValidation extends WithBindingFromParams {
  val notRequiredCap: FieldBinding = asInt("cap").greaterThan(100)

  val legalAge: FieldBinding = asInt("age").greaterThanOrEqualTo(18)
}


class ValidationSupportSpec extends Specification {
  implicit val formats: Formats = DefaultFormats
  import org.scalatra.util.ParamsValueReaderProperties._

  "The 'ValidationSupport' trait" should {

    "do normal binding within 'bindTo'" in {

      val ageValidatedForm = new WithValidation
      val params = Map("name" -> "John", "surname" -> "Doe", "age" -> "15")

      ageValidatedForm.bindTo(params)

      ageValidatedForm.a.value must_== params("name").toUpperCase.success
      ageValidatedForm.lower.value must_== params("surname").toLowerCase.success
      ageValidatedForm.age.value must_== 15.success

    }

    "validate only 'validatable bindings' within bindTo" in {

      val ageValidatedForm = new WithValidation
      val params = Map("name" -> "John", "surname" -> "Doe", "age" -> "15")

      ageValidatedForm.isValid must beTrue

      ageValidatedForm.bindTo(params)

      ageValidatedForm.isValid must beFalse

      ageValidatedForm.errors aka "validation error list" must have(_.name == "age")

      //ageValidatedForm.errors.get("age").get.asInstanceOf[Rejected[Int]] aka "the validation error" must_== (Rejected(Some("Your age must be at least of 18"), Some(15)))

      ageValidatedForm.legalAge.value aka "the validation result" must_== Failure(ValidationError("Age must be greater than or equal to 18", FieldName("age")))
      //ageValidatedForm.errors.filter(_.name == "age").head.validation aka "the validation result" must_== Failure(ValidationError("Your age must be at least of 18", 15))
    }


    "evaluate non-exaustive validation as 'accepted'" in {
      val formUnderTest = new WithValidation
      val params = Map("name" -> "John", "surname" -> "Doe", "age" -> "20")

      params must not haveKey ("cap")

      formUnderTest.bindTo(params)
      formUnderTest.isValid must beTrue

      formUnderTest.notRequiredCap.value must_== 0.success
      formUnderTest.notRequiredCap.isValid must beTrue
    }

  }
}


