import React, { useState } from "react";
import axios from "axios";
import { Form, Input, TextArea } from "semantic-ui-react";

const Post = ({ handleCancel2 }) => {
  const [form, setForm] = useState({
    title: "",
    content: "",
    postNum: "",
  });

  const onClickPost = () => {
    axios
      .post(
        "/mafiace/api/notice",
        { title: form.title, content: form.content },
        {
          headers: { "Content-Type": `application/json` },
        }
      )
      .then((res) => {})
      .catch((err) => {});
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({
      ...form,
      [name]: value,
    });
  };

  return (
    <div className="postbox h-screen w-full fixed left-0 top-0 flex justify-center items-center">
      <div className="bg-white rouded shadow-lg w-10 md:w-2/5">
        <div
          className="text-xl font-bold  text-center"
          style={{ margin: "5%" }}
        >
          Create
        </div>
        <hr></hr>
        <form>
          {/* <div>
              <label className="w-full flex-1 mx-2 text-xs font-semibold text-gray-600 uppercase">
                Title
                <input
                  required
                  placeholder="입력하세요"
                  type="text"
                  name="title"
                  value={form.title}
                  onChange={handleChange}
                  className="postInput"
                />
              </label>
              <label className="w-full flex-1 mx-2 text-xs font-semibold text-gray-600 uppercase">
                Content
                <input
                  required
                  placeholder="입력하세요"
                  type="text"
                  name="content"
                  value={form.content}
                  onChange={handleChange}
                  className=""
                />
              </label>
            </div> */}

          <Form style={{ textAlign: "center" }}>
            Title
            <Form.Group widths="equal">
              <Form.Field
                control={Input}
                placeholder="입력하세요"
                type="text"
                name="title"
                value={form.title}
                onChange={handleChange}
                style={{ width: "70%" }}
              />
            </Form.Group>
            Content
            <Form.Group inline></Form.Group>
            <Form.Field
              control={TextArea}
              placeholder="입력하세요"
              type="text"
              name="content"
              value={form.content}
              onChange={handleChange}
              style={{ width: "70%", height: "500px" }}
            />
          </Form>

          <div className="text-center">
            <button
              onClick={onClickPost}
              // onClick={handleSubmit}
              className="bg-purple-300 hover:bg-purple-500 py-2 text-center px-10 md:px-12 md:py-3 text-white rounded text-xl md:text-base mt-4"
              style={{ margin: "5%" }}
            >
              저장
            </button>

            <button
              onClick={handleCancel2}
              className="bg-purple-300 hover:bg-purple-500 py-2 text-center px-10 md:px-12 md:py-3 text-white rounded text-xl md:text-base mt-4"
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Post;
